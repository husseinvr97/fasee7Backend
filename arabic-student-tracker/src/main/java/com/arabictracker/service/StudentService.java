package com.arabictracker.service;

import com.arabictracker.dto.requestDto.CreateStudentRequest;
import com.arabictracker.dto.requestDto.UpdateStudentRequest;
import com.arabictracker.dto.responseDto.BehavioralIncidentResponse;
import com.arabictracker.dto.responseDto.DeletionResponse;
import com.arabictracker.dto.responseDto.ParentSummary;
import com.arabictracker.dto.responseDto.StudentResponse;
import com.arabictracker.model.BehavioralIncident;
import com.arabictracker.model.DeletionLog;
import com.arabictracker.model.LessonAttendance;
import com.arabictracker.model.Parent;
import com.arabictracker.model.Student;
import com.arabictracker.model.Student.StudentStatus;
import com.arabictracker.repository.*;
import com.arabictracker.util.ArabicNameUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {
    
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final DeletionLogRepository deletionLogRepository;
    private final ArabicNameUtils nameUtils;
    private final LessonAttendanceRepository attendanceRepository;
    private final BehavioralIncidentRepository incidentRepository;
    
    @Transactional
    public StudentResponse createStudent(CreateStudentRequest request) {
        // Clean phone numbers
        String cleanParentPhone = nameUtils.cleanPhoneNumber(request.getParentPhone());
        String cleanStudentPhone = nameUtils.cleanPhoneNumber(request.getStudentPhone());
        String cleanWhatsapp = nameUtils.cleanPhoneNumber(request.getParentWhatsapp());
        
        // Find or create parent
        Parent parent = parentRepository.findByPhone(cleanParentPhone)
            .orElseGet(() -> {
                Parent newParent = new Parent();
                newParent.setPhone(cleanParentPhone);
                newParent.setWhatsappNumber(cleanWhatsapp);
                newParent.setEmail(request.getParentEmail());
                newParent.setPreferredContactMethod(request.getPreferredContactMethod());
                
                // Auto-extract or use provided parent name
                String parentName = request.getParentFullName() != null 
                    ? request.getParentFullName()
                    : nameUtils.extractParentName(request.getStudentFullName());
                newParent.setFullName(parentName);
                
                return parentRepository.save(newParent);
            });
        
        // Create student
        Student student = new Student();
        student.setFullName(request.getStudentFullName());
        student.setFirstName(nameUtils.extractFirstName(request.getStudentFullName()));
        student.setFatherName(nameUtils.extractFatherName(request.getStudentFullName()));
        student.setSearchName(nameUtils.normalizeForSearch(request.getStudentFullName()));
        student.setStudentPhone(cleanStudentPhone);
        student.setParent(parent);
        student.setStatus(StudentStatus.ACTIVE);
        student.setEnrollmentDate(LocalDateTime.now());
        
        Student saved = studentRepository.save(student);
        return mapToStudentResponse(saved);
    }
    
    public List<StudentResponse> getActiveStudents() {
        return studentRepository.findByStatus(StudentStatus.ACTIVE).stream()
            .map(this::mapToStudentResponse)
            .collect(Collectors.toList());
    }

    public List<StudentResponse> getAllStudents() {
    return studentRepository.findAll().stream()
        .map(this::mapToStudentResponse)
        .collect(Collectors.toList());
}
    
    public List<StudentResponse> getArchivedStudents() {
        return studentRepository.findByStatus(StudentStatus.ARCHIVED).stream()
            .map(this::mapToStudentResponse)
            .collect(Collectors.toList());
    }
    
    public StudentResponse getStudentById(Long id) {
    Student student = studentRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Student not found"));
    return mapToStudentResponseEnhanced(student);
}


    
    public List<StudentResponse> searchStudents(String query) {
        String normalizedQuery = nameUtils.normalizeForSearch(query);
        return studentRepository.searchActiveStudents(normalizedQuery).stream()
            .map(this::mapToStudentResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public StudentResponse updateStudent(Long id, UpdateStudentRequest request) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Student not found"));
        
        student.setFullName(request.getFullName());
        student.setFirstName(nameUtils.extractFirstName(request.getFullName()));
        student.setFatherName(nameUtils.extractFatherName(request.getFullName()));
        student.setSearchName(nameUtils.normalizeForSearch(request.getFullName()));
        student.setStudentPhone(nameUtils.cleanPhoneNumber(request.getStudentPhone()));
        
        if (request.getStatus() != null) {
            student.setStatus(request.getStatus());
        }
        
        Student updated = studentRepository.save(student);
        return mapToStudentResponse(updated);
    }
    
    @Transactional
    public void archiveStudent(Long id) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Student not found"));
        
        student.setStatus(StudentStatus.ARCHIVED);
        student.setArchivedDate(LocalDateTime.now());
        studentRepository.save(student);
    }
    
    @Transactional
    public StudentResponse restoreStudent(Long id) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Student not found"));
        
        if (student.getStatus() != StudentStatus.ARCHIVED) {
            throw new RuntimeException("Student is not archived");
        }
        
        student.setStatus(StudentStatus.ACTIVE);
        student.setArchivedDate(null);
        Student restored = studentRepository.save(student);
        return mapToStudentResponse(restored);
    }
    
    @Transactional
    public DeletionResponse permanentlyDeleteStudent(Long id, String deletedBy) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Student not found"));
        
        // Log deletion
        DeletionLog log = new DeletionLog();
        log.setStudentId(student.getId());
        log.setStudentName(student.getFullName());
        log.setDeletedAt(LocalDateTime.now());
        log.setDeletedBy(deletedBy);
        log.setReason("Manual permanent deletion by admin");
        deletionLogRepository.save(log);
        
        // Delete student
        studentRepository.delete(student);
        
        DeletionResponse response = new DeletionResponse();
        response.setDeletedStudentId(log.getStudentId());
        response.setStudentName(log.getStudentName());
        response.setDeletedAt(log.getDeletedAt());
        response.setDeletedBy(log.getDeletedBy());
        return response;
    }
    
    private StudentResponse mapToStudentResponse(Student student) {
        StudentResponse response = new StudentResponse();
        response.setId(student.getId());
        response.setFullName(student.getFullName());
        response.setFirstName(student.getFirstName());
        response.setFatherName(student.getFatherName());
        response.setStudentPhone(student.getStudentPhone());
        response.setStatus(student.getStatus());
        response.setEnrollmentDate(student.getEnrollmentDate());
        response.setArchivedDate(student.getArchivedDate());
        response.setCreatedAt(student.getCreatedAt());
        response.setUpdatedAt(student.getUpdatedAt());
        
        ParentSummary parentSummary = new ParentSummary();
        parentSummary.setId(student.getParent().getId());
        parentSummary.setFullName(student.getParent().getFullName());
        parentSummary.setPhone(student.getParent().getPhone());
        parentSummary.setWhatsappNumber(student.getParent().getWhatsappNumber());
        parentSummary.setEmail(student.getParent().getEmail());
        parentSummary.setPreferredContactMethod(student.getParent().getPreferredContactMethod());
        response.setParent(parentSummary);
        
        return response;
    }

    private StudentResponse mapToStudentResponseEnhanced(Student student) {
    StudentResponse response = new StudentResponse();
    response.setId(student.getId());
    response.setFullName(student.getFullName());
    response.setFirstName(student.getFirstName());
    response.setFatherName(student.getFatherName());
    response.setStudentPhone(student.getStudentPhone());
    response.setStatus(student.getStatus());
    response.setEnrollmentDate(student.getEnrollmentDate());
    response.setArchivedDate(student.getArchivedDate());
    response.setCreatedAt(student.getCreatedAt());
    response.setUpdatedAt(student.getUpdatedAt());
    
    // Parent info
    ParentSummary parentSummary = new ParentSummary();
    parentSummary.setId(student.getParent().getId());
    parentSummary.setFullName(student.getParent().getFullName());
    parentSummary.setPhone(student.getParent().getPhone());
    parentSummary.setWhatsappNumber(student.getParent().getWhatsappNumber());
    parentSummary.setEmail(student.getParent().getEmail());
    parentSummary.setPreferredContactMethod(student.getParent().getPreferredContactMethod());
    response.setParent(parentSummary);
    
    // ✅ CALCULATE ATTENDANCE STATISTICS
    List<LessonAttendance> allAttendance = attendanceRepository.findByStudentAndDateRange(
        student.getId(),
        LocalDate.of(2020, 1, 1), // From beginning
        LocalDate.now()
    );
    
    if (!allAttendance.isEmpty()) {
        int totalLessons = allAttendance.size();
        long attended = allAttendance.stream()
            .filter(LessonAttendance::getAttended)
            .count();
        int absent = totalLessons - (int) attended;
        double percentage = totalLessons > 0 ? (attended * 100.0 / totalLessons) : 0.0;
        
        // Get consecutive absences from most recent lesson
        LessonAttendance mostRecent = allAttendance.stream()
            .max((a, b) -> a.getLesson().getDate().compareTo(b.getLesson().getDate()))
            .orElse(null);
        
        int consecutiveAbsences = mostRecent != null ? mostRecent.getConsecutiveAbsences() : 0;
        
        response.setTotalLessons(totalLessons);
        response.setTotalLessonsAttended((int) attended);
        response.setTotalLessonsAbsent(absent);
        response.setAttendancePercentage(Math.round(percentage * 100.0) / 100.0); // Round to 2 decimals
        response.setConsecutiveAbsences(consecutiveAbsences);
    } else {
        // No attendance records yet
        response.setTotalLessons(0);
        response.setTotalLessonsAttended(0);
        response.setTotalLessonsAbsent(0);
        response.setAttendancePercentage(0.0);
        response.setConsecutiveAbsences(0);
    }
    
    // ✅ GET BEHAVIORAL INCIDENTS
    List<BehavioralIncident> incidents = incidentRepository.findByStudentId(student.getId());
    
    List<BehavioralIncidentResponse> incidentResponses = incidents.stream()
        .map(incident -> {
            BehavioralIncidentResponse incidentResponse = new BehavioralIncidentResponse();
            incidentResponse.setId(incident.getId());
            incidentResponse.setLessonId(incident.getLesson().getId());
            incidentResponse.setStudentId(student.getId());
            incidentResponse.setIncidentType(incident.getIncidentType().name());
            incidentResponse.setTalkedWithStudentIds(incident.getTalkedWithStudentIds());
            incidentResponse.setNotes(incident.getNotes());
            incidentResponse.setCreatedAt(incident.getCreatedAt());
            
            // Calculate total incidents for the month of this incident
            YearMonth month = YearMonth.from(incident.getCreatedAt());
            Long totalIncidentsThisMonth = incidentRepository.countByStudentAndMonth(
                student.getId(), 
                month.getYear(), 
                month.getMonthValue()
            );
            incidentResponse.setTotalIncidentsThisMonth(totalIncidentsThisMonth);
            incidentResponse.setSpecialNotificationTriggered(totalIncidentsThisMonth >= 3);
            
            return incidentResponse;
        })
        .collect(Collectors.toList());
    
    response.setBehavioralIncidents(incidentResponses);
    
    return response;
}
}