package com.arabictracker.dto.responseDto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class DeletionResponse {
    private Long deletedStudentId;
    private String studentName;
    private LocalDateTime deletedAt;
    private String deletedBy;
}