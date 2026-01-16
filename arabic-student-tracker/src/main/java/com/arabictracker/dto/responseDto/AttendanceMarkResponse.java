package com.arabictracker.dto.responseDto;

import java.util.List;

import lombok.Data;

@Data
public class AttendanceMarkResponse {
    private Long lessonId;
    private Boolean attendanceMarked;
    private Integer attendedCount;
    private Integer absentCount;
    private List<AbsentStudentWarning> absentStudents;
}