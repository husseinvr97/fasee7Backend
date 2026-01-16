package com.arabictracker.dto.responseDto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class AttendanceRecordDto {
    private Long lessonId;
    private LocalDate date;
    private Boolean attended;
}