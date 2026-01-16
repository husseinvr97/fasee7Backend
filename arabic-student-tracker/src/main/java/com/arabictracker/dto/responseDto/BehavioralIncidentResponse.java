package com.arabictracker.dto.responseDto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class BehavioralIncidentResponse {
    private Long id;
    private Long lessonId;
    private Long studentId;
    private String incidentType;
    private List<Long> talkedWithStudentIds;
    private String notes;
    private Long totalIncidentsThisMonth;
    private Boolean specialNotificationTriggered;
    private LocalDateTime createdAt;
}