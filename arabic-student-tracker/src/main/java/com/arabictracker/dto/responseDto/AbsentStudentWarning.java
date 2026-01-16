package com.arabictracker.dto.responseDto;

import lombok.Data;

@Data
public class AbsentStudentWarning {
    private Long studentId;
    private String studentName;
    private Integer consecutiveAbsences;
    private Boolean warningTriggered;
}