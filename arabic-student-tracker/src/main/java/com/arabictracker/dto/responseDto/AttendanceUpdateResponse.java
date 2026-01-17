package com.arabictracker.dto.responseDto;

import lombok.Data;

@Data
public class AttendanceUpdateResponse {
    private Long lessonId;
    private Long studentId;
    private String studentName;
    private Boolean attended;
    private Integer consecutiveAbsences;
    private String message;
}