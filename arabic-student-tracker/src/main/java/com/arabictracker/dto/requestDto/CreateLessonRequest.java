package com.arabictracker.dto.requestDto;

import com.arabictracker.model.Lesson.CategoryTag;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

// ============ REQUEST DTOs ============

@Data
public class CreateLessonRequest {
    @NotNull(message = "Date is required")
    private LocalDate date;
    
    private String topicsFreeText;
    
    private List<CategoryTag> categoryTags;
    
    @NotNull(message = "hasHomework is required")
    private Boolean hasHomework;
}