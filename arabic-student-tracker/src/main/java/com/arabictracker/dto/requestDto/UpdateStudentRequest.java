package com.arabictracker.dto.requestDto;

import com.arabictracker.model.Student.StudentStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateStudentRequest {
    @NotBlank(message = "Student full name is required")
    private String fullName;
    
    @Pattern(regexp = "^01[0-9]{9}$", message = "Invalid Egyptian phone format")
    private String studentPhone;
    
    private StudentStatus status;
}