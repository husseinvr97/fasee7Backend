package com.arabictracker.dto.responseDto;

import java.time.LocalDateTime;

import com.arabictracker.model.Student.StudentStatus;

import lombok.Data;

@Data
public class StudentResponse {
    private Long id;
    private String fullName;
    private String firstName;
    private String fatherName;
    private String studentPhone;
    private StudentStatus status;
    private LocalDateTime enrollmentDate;
    private LocalDateTime archivedDate;
    private ParentSummary parent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}