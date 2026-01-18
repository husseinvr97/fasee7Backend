package com.arabictracker.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "behavioral_incidents",
    indexes = {
        @Index(name = "idx_incident_lesson", columnList = "lesson_id"),
        @Index(name = "idx_incident_student", columnList = "student_id"),
        @Index(name = "idx_incident_created", columnList = "created_at")
    })
@EntityListeners(AuditingEntityListener.class)
@Data
public class BehavioralIncident {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentType incidentType;
    
    @ElementCollection
    @CollectionTable(name = "incident_talked_with_students", 
                     joinColumns = @JoinColumn(name = "incident_id"))
    @Column(name = "talked_with_student_id")
    private List<Long> talkedWithStudentIds;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public enum IncidentType {
    TALKS_WITH_OTHERS,
    DISRUPTIVE,
    DISRESPECTFUL,
    LATE,
    LEFT_EARLIER  // ← ADD THIS NEW TYPE
}
}