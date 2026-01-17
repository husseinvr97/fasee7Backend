package com.arabictracker.dto.requestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAttendanceRequest {
    @NotNull(message = "Attended status is required")
    private Boolean attended;
}