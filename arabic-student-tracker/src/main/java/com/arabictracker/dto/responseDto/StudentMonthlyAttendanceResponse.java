package com.arabictracker.dto.responseDto;

import java.util.List;

import lombok.Data;

@Data
public class StudentMonthlyAttendanceResponse {
    private Long studentId;
    private String studentName;
    private Integer year;
    private Integer month;
    private Integer totalLessons;
    private Integer attended;
    private Integer absent;
    private Double attendancePercentage;
    private List<LessonDetailDto> lessonDetails;
}