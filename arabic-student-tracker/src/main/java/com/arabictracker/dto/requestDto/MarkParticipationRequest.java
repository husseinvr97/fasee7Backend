package com.arabictracker.dto.requestDto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class MarkParticipationRequest {
    @NotEmpty(message = "participationScores cannot be empty")
    private List<ParticipationScoreDto> participationScores;
}