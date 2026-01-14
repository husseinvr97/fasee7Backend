package com.arabictracker.dto.responseDto;

import java.time.LocalDateTime;

import com.arabictracker.model.Student.StudentStatus;

import lombok.Data;

@Data
public class StudentSummary {
    private Long id;
    private String fullName;
    private StudentStatus status;
    private LocalDateTime enrollmentDate;
}