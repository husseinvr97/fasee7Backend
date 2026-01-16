package com.arabictracker.dto.responseDto;

import java.util.List;

import lombok.Data;

@Data
public class AttendanceWarningResponse {
    private List<StudentWarningDto> warnings;
}