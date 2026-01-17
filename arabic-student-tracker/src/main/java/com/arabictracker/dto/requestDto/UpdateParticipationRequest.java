package com.arabictracker.dto.requestDto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateParticipationRequest {
    @NotNull(message = "Score is required")
    @Min(value = 0, message = "Score must be between 0 and 5")
    @Max(value = 5, message = "Score must be between 0 and 5")
    private Integer score;
}