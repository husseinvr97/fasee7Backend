package com.arabictracker.dto.requestDto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class MarkAttendanceRequest {
    @NotEmpty(message = "attendedStudentIds cannot be empty")
    private List<Long> attendedStudentIds;
}