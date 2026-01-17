package com.arabictracker.dto.requestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateHomeworkRequest {
    @NotNull(message = "Completed status is required")
    private Boolean completed;
}