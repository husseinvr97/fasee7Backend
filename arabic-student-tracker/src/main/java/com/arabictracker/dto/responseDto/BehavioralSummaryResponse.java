package com.arabictracker.dto.responseDto;

import java.util.List;

import lombok.Data;

@Data
public class BehavioralSummaryResponse {
    private Long studentId;
    private String studentName;
    private String month;
    private Long totalIncidents;
    private String behavioralLevel;
    private java.util.Map<String, Long> incidentsByType;
    private List<MostTalkedWithDto> mostTalkedWith;
    private Boolean specialNotificationSent;
}