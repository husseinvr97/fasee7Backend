package com.arabictracker.service;

import com.arabictracker.dto.requestDto.CreateLessonRequest;
import com.arabictracker.dto.requestDto.UpdateLessonRequest;
import com.arabictracker.dto.responseDto.AbsentStudentDto;
import com.arabictracker.dto.responseDto.AttendedStudentDto;
import com.arabictracker.dto.responseDto.LessonDetailResponse;
import com.arabictracker.dto.responseDto.LessonResponse;
import com.arabictracker.model.*;
import com.arabictracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {
    
    private final LessonRepository lessonRepository;
    private final LessonAttendanceRepository attendanceRepository;
    private final LessonHomeworkRepository homeworkRepository;
    private final LessonParticipationRepository participationRepository;
    private final StudentRepository studentRepository;
    
    @Transactional
    public LessonResponse createLesson(CreateLessonRequest request) {
        // Check if lesson already exists for this date
        lessonRepository.findByDateAndDeletedAtIsNull(request.getDate())
            .ifPresent(lesson -> {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Lesson already exists for date: " + request.getDate()
                );
            });
        
        Lesson lesson = new Lesson();
        lesson.setDate(request.getDate());
        lesson.setTopicsFreeText(request.getTopicsFreeText());
        lesson.setCategoryTags(request.getCategoryTags());
        lesson.setHasHomework(request.getHasHomework());
        
        Lesson saved = lessonRepository.save(lesson);
        return mapToLessonResponse(saved);
    }
    
    public List<LessonResponse> getAllLessons(LocalDate startDate, LocalDate endDate) {
        List<Lesson> lessons;
        
        if (startDate != null && endDate != null) {
            lessons = lessonRepository.findByDateBetweenAndDeletedAtIsNull(startDate, endDate);
        } else {
            lessons = lessonRepository.findByDeletedAtIsNull();
        }
        
        return lessons.stream()
            .map(this::mapToLessonResponse)
            .collect(Collectors.toList());
    }
    
    public LessonDetailResponse getLessonById(Long lessonId) {
        Lesson lesson = lessonRepository.findByIdAndDeletedAtIsNull(lessonId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Lesson not found"
            ));
        
        return mapToLessonDetailResponse(lesson);
    }
    
    @Transactional
    public LessonResponse updateLesson(Long lessonId, UpdateLessonRequest request) {
        Lesson lesson = lessonRepository.findByIdAndDeletedAtIsNull(lessonId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Lesson not found"
            ));
        
        // Check if changing date conflicts with another lesson
        if (!lesson.getDate().equals(request.getDate())) {
            lessonRepository.findByDateAndDeletedAtIsNull(request.getDate())
                .ifPresent(existingLesson -> {
                    throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Another lesson already exists for date: " + request.getDate()
                    );
                });
        }
        
        lesson.setDate(request.getDate());
        lesson.setTopicsFreeText(request.getTopicsFreeText());
        lesson.setCategoryTags(request.getCategoryTags());
        lesson.setHasHomework(request.getHasHomework());
        
        Lesson updated = lessonRepository.save(lesson);
        return mapToLessonResponse(updated);
    }
    
    @Transactional
    public void deleteLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findByIdAndDeletedAtIsNull(lessonId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Lesson not found"
            ));
        
        lesson.setDeletedAt(java.time.LocalDateTime.now());
        lessonRepository.save(lesson);
    }
    
    private LessonResponse mapToLessonResponse(Lesson lesson) {
        LessonResponse response = new LessonResponse();
        response.setId(lesson.getId());
        response.setDate(lesson.getDate());
        response.setTopicsFreeText(lesson.getTopicsFreeText());
        response.setCategoryTags(lesson.getCategoryTags());
        response.setHasHomework(lesson.getHasHomework());
        response.setAttendanceMarked(lesson.getAttendanceMarked());
        response.setHomeworkMarked(lesson.getHomeworkMarked());
        response.setParticipationMarked(lesson.getParticipationMarked());
        
        // Calculate attendance statistics
        List<LessonAttendance> attendanceList = attendanceRepository.findByLessonId(lesson.getId());
        long attendedCount = attendanceList.stream().filter(LessonAttendance::getAttended).count();
        response.setAttendanceCount((int) attendedCount);
        
        // Total active students
        long totalActive = studentRepository.findByStatus(Student.StudentStatus.ACTIVE).size();
        response.setTotalStudents((int) totalActive);
        
        response.setCreatedAt(lesson.getCreatedAt());
        response.setUpdatedAt(lesson.getUpdatedAt());
        
        return response;
    }
    
    private LessonDetailResponse mapToLessonDetailResponse(Lesson lesson) {
        LessonDetailResponse response = new LessonDetailResponse();
        response.setId(lesson.getId());
        response.setDate(lesson.getDate());
        response.setTopicsFreeText(lesson.getTopicsFreeText());
        response.setCategoryTags(lesson.getCategoryTags());
        response.setHasHomework(lesson.getHasHomework());
        response.setAttendanceMarked(lesson.getAttendanceMarked());
        response.setHomeworkMarked(lesson.getHomeworkMarked());
        response.setParticipationMarked(lesson.getParticipationMarked());
        response.setCreatedAt(lesson.getCreatedAt());
        response.setUpdatedAt(lesson.getUpdatedAt());
        
        // Get attendance records
        List<LessonAttendance> attendanceList = attendanceRepository.findByLessonId(lesson.getId());
        
        // Attended students
        List<AttendedStudentDto> attendedStudents = attendanceList.stream()
            .filter(LessonAttendance::getAttended)
            .map(att -> {
                AttendedStudentDto dto = new AttendedStudentDto();
                dto.setStudentId(att.getStudent().getId());
                dto.setStudentName(att.getStudent().getFullName());
                
                // Get homework status
                homeworkRepository.findByLessonIdAndStudentId(lesson.getId(), att.getStudent().getId())
                    .ifPresent(hw -> dto.setHomeworkCompleted(hw.getCompleted()));
                
                // Get participation score
                participationRepository.findByLessonIdAndStudentId(lesson.getId(), att.getStudent().getId())
                    .ifPresent(p -> dto.setParticipationScore(p.getScore()));
                
                return dto;
            })
            .collect(Collectors.toList());
        response.setAttendedStudents(attendedStudents);
        
        // Absent students
        List<AbsentStudentDto> absentStudents = attendanceList.stream()
            .filter(att -> !att.getAttended())
            .map(att -> {
                AbsentStudentDto dto = new AbsentStudentDto();
                dto.setStudentId(att.getStudent().getId());
                dto.setStudentName(att.getStudent().getFullName());
                dto.setConsecutiveAbsences(att.getConsecutiveAbsences());
                return dto;
            })
            .collect(Collectors.toList());
        response.setAbsentStudents(absentStudents);
        
        return response;
    }
}