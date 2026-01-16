package com.arabictracker.dto.requestDto;

import com.arabictracker.model.Parent.ContactMethod;

import lombok.Data;
import jakarta.validation.constraints.*;

// ============ REQUEST DTOs ============

@Data
public class CreateStudentRequest {
    @NotBlank(message = "Student full name is required")
    private String studentFullName;
    
    @Pattern(regexp = "^01[0-9]{9}$", message = "Invalid Egyptian phone format")
    private String studentPhone; // optional
    
    @NotBlank(message = "Parent phone is required")
    @Pattern(regexp = "^01[0-9]{9}$", message = "Invalid Egyptian phone format")
    private String parentPhone;
    
    @Pattern(regexp = "^01[0-9]{9}$", message = "Invalid Egyptian phone format")
    private String parentWhatsapp; // optional
    
    @Email(message = "Invalid email format")
    private String parentEmail; // optional
    
    private ContactMethod preferredContactMethod = ContactMethod.WHATSAPP;
    
    private String parentFullName; // optional, auto-extracted if not provided
}

