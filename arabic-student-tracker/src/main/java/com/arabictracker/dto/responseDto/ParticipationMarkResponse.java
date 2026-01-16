package com.arabictracker.dto.responseDto;

import lombok.Data;

@Data
public class ParticipationMarkResponse {
    private Long lessonId;
    private Boolean participationMarked;
    private Integer studentsScored;
}