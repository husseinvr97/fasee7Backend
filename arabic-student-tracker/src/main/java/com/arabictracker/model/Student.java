package com.arabictracker.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "students", indexes = {
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_archived_date", columnList = "archived_date"),
    @Index(name = "idx_search_name", columnList = "search_name")
})
@EntityListeners(AuditingEntityListener.class)
@Data
public class Student {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String fullName;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String fatherName;
    
    @Column(nullable = false)
    private String searchName;
    
    @Column(length = 11)
    private String studentPhone;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private Parent parent;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudentStatus status = StudentStatus.ACTIVE;
    
    @Column
    private LocalDateTime archivedDate;
    
    @Column(nullable = false)
    private LocalDateTime enrollmentDate;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum StudentStatus {
        ACTIVE, ARCHIVED
    }
}