package com.arabictracker.dto.requestDto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class MarkHomeworkRequest {
    @NotEmpty(message = "completedStudentIds cannot be empty")
    private List<Long> completedStudentIds;
}