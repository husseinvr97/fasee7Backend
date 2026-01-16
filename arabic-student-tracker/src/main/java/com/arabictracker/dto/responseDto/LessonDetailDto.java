package com.arabictracker.dto.responseDto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class LessonDetailDto {
    private Long lessonId;
    private LocalDate date;
    private Boolean attended;
    private Boolean homeworkCompleted;
    private Integer participationScore;
    private Integer consecutiveAbsences;
}