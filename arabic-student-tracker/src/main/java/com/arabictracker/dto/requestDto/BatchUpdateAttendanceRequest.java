package com.arabictracker.dto.requestDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BatchUpdateAttendanceRequest {
    
    @NotEmpty(message = "Updates list cannot be empty")
    @Valid
    private List<AttendanceUpdate> updates;
    
    @Data
    public static class AttendanceUpdate {
        @NotNull(message = "Student ID is required")
        private Long studentId;
        
        @NotNull(message = "Attended status is required")
        private Boolean attended;
    }
}