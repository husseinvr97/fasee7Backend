package com.arabictracker.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "lessons", indexes = {
    @Index(name = "idx_lesson_date", columnList = "date"),
    @Index(name = "idx_deleted_at", columnList = "deleted_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
public class Lesson {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private LocalDate date;
    
    @Column(columnDefinition = "TEXT")
    private String topicsFreeText;
    
    @ElementCollection(targetClass = CategoryTag.class)
    @CollectionTable(name = "lesson_category_tags", joinColumns = @JoinColumn(name = "lesson_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "category_tag")
    private List<CategoryTag> categoryTags;
    
    @Column(nullable = false)
    private Boolean hasHomework = false;
    
    @Column(nullable = false)
    private Boolean attendanceMarked = false;
    
    @Column(nullable = false)
    private Boolean homeworkMarked = false;
    
    @Column(nullable = false)
    private Boolean participationMarked = false;
    
    @Column
    private LocalDateTime deletedAt;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum CategoryTag {
        NAHW("نحو"),
        ADAB("أدب"),
        BALAGHA("بلاغة"),
        TABEER("تعبير"),
        QIRAA("قراءة/فهم مقروء"),
        NUSUS("نصوص");
        
        private final String arabicName;
        
        CategoryTag(String arabicName) {
            this.arabicName = arabicName;
        }
        
        public String getArabicName() {
            return arabicName;
        }
    }
}