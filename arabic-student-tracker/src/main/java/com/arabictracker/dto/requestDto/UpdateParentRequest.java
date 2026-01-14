package com.arabictracker.dto.requestDto;



import com.arabictracker.model.Parent.ContactMethod;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateParentRequest {
    @NotBlank(message = "Parent full name is required")
    private String fullName;
    
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^01[0-9]{9}$", message = "Invalid Egyptian phone format")
    private String phone;
    
    @Pattern(regexp = "^01[0-9]{9}$", message = "Invalid Egyptian phone format")
    private String whatsappNumber;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private ContactMethod preferredContactMethod;
}