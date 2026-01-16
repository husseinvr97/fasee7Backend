package com.arabictracker.service;


import com.arabictracker.dto.requestDto.CreateBehavioralIncidentRequest;
import com.arabictracker.dto.responseDto.BehavioralIncidentResponse;
import com.arabictracker.dto.responseDto.BehavioralSummaryResponse;
import com.arabictracker.dto.responseDto.MostTalkedWithDto;
import com.arabictracker.model.*;
import com.arabictracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BehavioralService {
    
    private final BehavioralIncidentRepository incidentRepository;
    private final LessonRepository lessonRepository;
    private final StudentRepository studentRepository;
    
    @Transactional
    public BehavioralIncidentResponse createBehavioralIncident(
            Long lessonId, CreateBehavioralIncidentRequest request) {
        
        Lesson lesson = lessonRepository.findByIdAndDeletedAtIsNull(lessonId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Lesson not found"
            ));
        
        Student student = studentRepository.findById(request.getStudentId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Student not found"
            ));
        
        BehavioralIncident incident = new BehavioralIncident();
        incident.setLesson(lesson);
        incident.setStudent(student);
        
        try {
            incident.setIncidentType(BehavioralIncident.IncidentType.valueOf(request.getIncidentType()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Invalid incident type. Must be one of: TALKS_WITH_OTHERS, DISRUPTIVE, DISRESPECTFUL, LATE"
            );
        }
        
        incident.setTalkedWithStudentIds(request.getTalkedWithStudentIds());
        incident.setNotes(request.getNotes());
        
        BehavioralIncident saved = incidentRepository.save(incident);
        
        // Calculate total incidents this month
        YearMonth currentMonth = YearMonth.from(saved.getCreatedAt());
        Long totalIncidents = incidentRepository.countByStudentAndMonth(
            student.getId(), currentMonth.getYear(), currentMonth.getMonthValue()
        );
        
        // Check if special notification should trigger (first time hitting 3)
        boolean specialNotificationTriggered = totalIncidents == 3;
        
        BehavioralIncidentResponse response = new BehavioralIncidentResponse();
        response.setId(saved.getId());
        response.setLessonId(lessonId);
        response.setStudentId(student.getId());
        response.setIncidentType(saved.getIncidentType().name());
        response.setTalkedWithStudentIds(saved.getTalkedWithStudentIds());
        response.setNotes(saved.getNotes());
        response.setTotalIncidentsThisMonth(totalIncidents);
        response.setSpecialNotificationTriggered(specialNotificationTriggered);
        response.setCreatedAt(saved.getCreatedAt());
        
        return response;
    }
    
    public BehavioralSummaryResponse getStudentBehavioralSummary(Long studentId, String month) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Student not found"
            ));
        
        // Parse month (format: YYYY-MM)
        YearMonth yearMonth = YearMonth.parse(month);
        int year = yearMonth.getYear();
        int monthValue = yearMonth.getMonthValue();
        
        List<BehavioralIncident> incidents = incidentRepository.findByStudentAndMonth(
            studentId, year, monthValue
        );
        
        long totalIncidents = incidents.size();
        
        // Calculate behavioral level
        String behavioralLevel = calculateBehavioralLevel(totalIncidents);
        
        // Group incidents by type
        Map<String, Long> incidentsByType = incidents.stream()
            .collect(Collectors.groupingBy(
                inc -> inc.getIncidentType().name(),
                Collectors.counting()
            ));
        
        // Calculate most talked with students
        List<MostTalkedWithDto> mostTalkedWith = calculateMostTalkedWith(incidents);
        
        // Check if special notification was sent (3+ incidents)
        boolean specialNotificationSent = totalIncidents >= 3;
        
        BehavioralSummaryResponse response = new BehavioralSummaryResponse();
        response.setStudentId(studentId);
        response.setStudentName(student.getFullName());
        response.setMonth(month);
        response.setTotalIncidents(totalIncidents);
        response.setBehavioralLevel(behavioralLevel);
        response.setIncidentsByType(incidentsByType);
        response.setMostTalkedWith(mostTalkedWith);
        response.setSpecialNotificationSent(specialNotificationSent);
        
        return response;
    }
    
    private String calculateBehavioralLevel(long incidentCount) {
        if (incidentCount == 0) {
            return "EXEMPLARY";
        } else if (incidentCount <= 2) {
            return "GOOD";
        } else if (incidentCount <= 5) {
            return "MINOR_ISSUES";
        } else if (incidentCount <= 10) {
            return "BEHAVIORAL_CONCERNS";
        } else {
            return "SERIOUS_ISSUES";
        }
    }
    
    private List<MostTalkedWithDto> calculateMostTalkedWith(List<BehavioralIncident> incidents) {
        // Count occurrences of each student in talkedWithStudentIds
        Map<Long, Long> talkedWithCounts = new HashMap<>();
        
        for (BehavioralIncident incident : incidents) {
            if (incident.getTalkedWithStudentIds() != null) {
                for (Long studentId : incident.getTalkedWithStudentIds()) {
                    talkedWithCounts.merge(studentId, 1L, Long::sum);
                }
            }
        }
        
        // Convert to DTOs and sort by count descending
        return talkedWithCounts.entrySet().stream()
            .map(entry -> {
                Student student = studentRepository.findById(entry.getKey())
                    .orElse(null);
                
                if (student == null) return null;
                
                MostTalkedWithDto dto = new MostTalkedWithDto();
                dto.setStudentId(entry.getKey());
                dto.setStudentName(student.getFullName());
                dto.setCount(entry.getValue());
                return dto;
            })
            .filter(Objects::nonNull)
            .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
            .collect(Collectors.toList());
    }
}