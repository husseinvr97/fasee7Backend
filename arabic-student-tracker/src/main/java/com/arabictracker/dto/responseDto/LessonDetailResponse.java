package com.arabictracker.dto.responseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.arabictracker.model.Lesson.CategoryTag;

import lombok.Data;

@Data
public class LessonDetailResponse {
    private Long id;
    private LocalDate date;
    private String topicsFreeText;
    private List<CategoryTag> categoryTags;
    private Boolean hasHomework;
    private Boolean attendanceMarked;
    private Boolean homeworkMarked;
    private Boolean participationMarked;
    private List<AttendedStudentDto> attendedStudents;
    private List<AbsentStudentDto> absentStudents;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}