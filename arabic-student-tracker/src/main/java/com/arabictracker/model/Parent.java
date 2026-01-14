package com.arabictracker.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "parents")
@EntityListeners(AuditingEntityListener.class)
@Data
public class Parent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String fullName;
    
    @Column(nullable = false, unique = true, length = 11)
    private String phone;
    
    @Column(length = 11)
    private String whatsappNumber;
    
    @Column
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactMethod preferredContactMethod = ContactMethod.WHATSAPP;
    
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Student> students;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum ContactMethod {
        WHATSAPP, EMAIL
    }
}