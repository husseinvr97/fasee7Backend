package com.arabictracker.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "lesson_participation",
    uniqueConstraints = @UniqueConstraint(columnNames = {"lesson_id", "student_id"}),
    indexes = {
        @Index(name = "idx_participation_lesson", columnList = "lesson_id"),
        @Index(name = "idx_participation_student", columnList = "student_id")
    })
@EntityListeners(AuditingEntityListener.class)
@Data
public class LessonParticipation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @Column(nullable = false)
    private Integer score; // 0-5
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}