package com.arabictracker.service;

import com.arabictracker.dto.requestDto.CreateStudentRequest;
import com.arabictracker.dto.requestDto.UpdateStudentRequest;
import com.arabictracker.dto.responseDto.DeletionResponse;
import com.arabictracker.dto.responseDto.ParentSummary;
import com.arabictracker.dto.responseDto.StudentResponse;
import com.arabictracker.model.DeletionLog;
import com.arabictracker.model.Parent;
import com.arabictracker.model.Student;
import com.arabictracker.model.Student.StudentStatus;
import com.arabictracker.repository.*;
import com.arabictracker.util.ArabicNameUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {
    
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final DeletionLogRepository deletionLogRepository;
    private final ArabicNameUtils nameUtils;
    
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
    
    public List<StudentResponse> getArchivedStudents() {
        return studentRepository.findByStatus(StudentStatus.ARCHIVED).stream()
            .map(this::mapToStudentResponse)
            .collect(Collectors.toList());
    }
    
    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Student not found"));
        return mapToStudentResponse(student);
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
}