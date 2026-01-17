package com.arabictracker.dto.responseDto;

import lombok.Data;

@Data
public class HomeworkUpdateResponse {
    private Long lessonId;
    private Long studentId;
    private String studentName;
    private Boolean completed;
    private String message;
}