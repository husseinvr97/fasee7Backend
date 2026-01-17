package com.arabictracker.dto.responseDto;

import lombok.Data;

import java.util.List;

@Data
public class BatchAttendanceUpdateResponse {
    private Long lessonId;
    private Integer updatedCount;
    private List<AttendanceUpdateResponse> updates;
    private String message;
}