package com.arabictracker.service;

import com.arabictracker.dto.requestDto.UpdateParentRequest;
import com.arabictracker.dto.responseDto.ParentSummary;
import com.arabictracker.dto.responseDto.ParentWithStudentsResponse;
import com.arabictracker.dto.responseDto.StudentSummary;
import com.arabictracker.model.Parent;
import com.arabictracker.model.Student;
import com.arabictracker.repository.ParentRepository;
import com.arabictracker.util.ArabicNameUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParentService {
    
    private final ParentRepository parentRepository;
    private final ArabicNameUtils nameUtils;
    
    public ParentWithStudentsResponse getParentWithStudents(Long parentId) {
        Parent parent = parentRepository.findById(parentId)
            .orElseThrow(() -> new RuntimeException("Parent not found"));
        
        ParentWithStudentsResponse response = new ParentWithStudentsResponse();
        response.setId(parent.getId());
        response.setFullName(parent.getFullName());
        response.setPhone(parent.getPhone());
        response.setWhatsappNumber(parent.getWhatsappNumber());
        response.setEmail(parent.getEmail());
        response.setPreferredContactMethod(parent.getPreferredContactMethod());
        response.setCreatedAt(parent.getCreatedAt());
        response.setUpdatedAt(parent.getUpdatedAt());
        
        List<StudentSummary> students = parent.getStudents().stream()
            .map(this::mapToStudentSummary)
            .collect(Collectors.toList());
        response.setStudents(students);
        
        return response;
    }
    
    @Transactional
    public ParentSummary updateParent(Long parentId, UpdateParentRequest request) {
        Parent parent = parentRepository.findById(parentId)
            .orElseThrow(() -> new RuntimeException("Parent not found"));
        
        parent.setFullName(request.getFullName());
        parent.setPhone(nameUtils.cleanPhoneNumber(request.getPhone()));
        parent.setWhatsappNumber(nameUtils.cleanPhoneNumber(request.getWhatsappNumber()));
        parent.setEmail(request.getEmail());
        parent.setPreferredContactMethod(request.getPreferredContactMethod());
        
        Parent updated = parentRepository.save(parent);
        return mapToParentSummary(updated);
    }
    
    private ParentSummary mapToParentSummary(Parent parent) {
        ParentSummary summary = new ParentSummary();
        summary.setId(parent.getId());
        summary.setFullName(parent.getFullName());
        summary.setPhone(parent.getPhone());
        summary.setWhatsappNumber(parent.getWhatsappNumber());
        summary.setEmail(parent.getEmail());
        summary.setPreferredContactMethod(parent.getPreferredContactMethod());
        return summary;
    }
    
    private StudentSummary mapToStudentSummary(Student student) {
        StudentSummary summary = new StudentSummary();
        summary.setId(student.getId());
        summary.setFullName(student.getFullName());
        summary.setStatus(student.getStatus());
        summary.setEnrollmentDate(student.getEnrollmentDate());
        return summary;
    }
}