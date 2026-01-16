package com.arabictracker.dto.responseDto;

import java.util.List;

import lombok.Data;

@Data
public class StudentAttendanceHistoryResponse {
    private Long studentId;
    private String studentName;
    private Integer totalLessons;
    private Integer attended;
    private Integer absent;
    private Double attendancePercentage;
    private Integer consecutiveAbsences;
    private List<AttendanceRecordDto> attendanceRecords;
}