package com.arabictracker.dto.requestDto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBehavioralIncidentRequest {
    @NotNull(message = "studentId is required")
    private Long studentId;
    
    @NotNull(message = "incidentType is required")
    private String incidentType; // Will be converted to enum
    
    private List<Long> talkedWithStudentIds;
    
    private String notes;
}