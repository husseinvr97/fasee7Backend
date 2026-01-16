package com.arabictracker.dto.requestDto;

import java.time.LocalDate;
import java.util.List;

import com.arabictracker.model.Lesson.CategoryTag;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateLessonRequest {
    @NotNull(message = "Date is required")
    private LocalDate date;
    
    private String topicsFreeText;
    
    private List<CategoryTag> categoryTags;
    
    @NotNull(message = "hasHomework is required")
    private Boolean hasHomework;
}