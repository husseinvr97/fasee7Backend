package com.arabictracker.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "lesson_attendance", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"lesson_id", "student_id"}),
    indexes = {
        @Index(name = "idx_attendance_lesson", columnList = "lesson_id"),
        @Index(name = "idx_attendance_student", columnList = "student_id")
    })
@EntityListeners(AuditingEntityListener.class)
@Data
public class LessonAttendance {
    
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
    private Boolean attended;
    
    @Column(nullable = false)
    private Integer consecutiveAbsences = 0;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}