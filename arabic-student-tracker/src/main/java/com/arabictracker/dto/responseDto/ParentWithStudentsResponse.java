package com.arabictracker.dto.responseDto;

import java.time.LocalDateTime;
import java.util.List;

import com.arabictracker.model.Parent.ContactMethod;

import lombok.Data;

@Data
public class ParentWithStudentsResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String whatsappNumber;
    private String email;
    private ContactMethod preferredContactMethod;
    private List<StudentSummary> students;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}