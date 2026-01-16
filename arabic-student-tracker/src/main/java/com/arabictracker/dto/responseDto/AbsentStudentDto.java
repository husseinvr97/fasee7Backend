package com.arabictracker.dto.responseDto;

import lombok.Data;

@Data
public class AbsentStudentDto {
    private Long studentId;
    private String studentName;
    private Integer consecutiveAbsences;
}