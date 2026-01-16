package com.arabictracker.dto.responseDto;

import lombok.Data;

@Data
public class AttendedStudentDto {
    private Long studentId;
    private String studentName;
    private Boolean homeworkCompleted;
    private Integer participationScore;
}