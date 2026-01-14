package com.arabictracker.dto.responseDto;

import com.arabictracker.model.Parent.ContactMethod;

import lombok.Data;

@Data
public class ParentSummary {
    private Long id;
    private String fullName;
    private String phone;
    private String whatsappNumber;
    private String email;
    private ContactMethod preferredContactMethod;
}