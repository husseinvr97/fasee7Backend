package com.arabictracker.dto.responseDto;

import lombok.Data;

@Data
public class StudentWarningDto {
    private Long studentId;
    private String studentName;
    private Integer consecutiveAbsences;
    private String warningType; // TWO_ABSENCES or DELETION_TRIGGER
    private String parentPhone;
    private String parentPreferredContact;
}