package com.arabictracker.dto.responseDto;

import lombok.Data;

@Data
public class MostTalkedWithDto {
    private Long studentId;
    private String studentName;
    private Long count;
}