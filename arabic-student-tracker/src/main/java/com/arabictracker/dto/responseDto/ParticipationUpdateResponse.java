package com.arabictracker.dto.responseDto;

import lombok.Data;

@Data
public class ParticipationUpdateResponse {
    private Long lessonId;
    private Long studentId;
    private String studentName;
    private Integer score;
    private String message;
}