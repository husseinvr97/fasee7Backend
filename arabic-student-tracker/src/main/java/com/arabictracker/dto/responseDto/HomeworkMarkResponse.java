package com.arabictracker.dto.responseDto;

import lombok.Data;

@Data
public class HomeworkMarkResponse {
    private Long lessonId;
    private Boolean homeworkMarked;
    private Integer completedCount;
    private Integer notCompletedCount;
    private String warning;
}