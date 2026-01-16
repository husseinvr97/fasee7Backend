package com.arabictracker.service;

import com.arabictracker.model.DeletionLog;
import com.arabictracker.model.Lesson;
import com.arabictracker.model.Student;
import com.arabictracker.repository.*;
import com.arabictracker.repository.DeletionLogRepository;
import com.arabictracker.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledCleanupService {
    
    private static final Logger logger = LoggerFactory.getLogger(ScheduledCleanupService.class);
    
    private final StudentRepository studentRepository;
    private final DeletionLogRepository deletionLogRepository;
    private final LessonRepository lessonRepository;
    private final LessonAttendanceRepository attendanceRepository;
    private final LessonHomeworkRepository homeworkRepository;
    private final LessonParticipationRepository participationRepository;
    private final BehavioralIncidentRepository behavioralIncidentRepository;
    
    /**
     * Runs daily at 2 AM to:
     * 1. Permanently delete students archived for 30+ days
     * 2. Permanently delete lessons soft-deleted for 7+ days
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredArchivedStudents() {
        logger.info("Starting scheduled cleanup job");
        
        // Cleanup students (30 days)
        cleanupStudents();
        
        // Cleanup lessons (7 days)
        cleanupLessons();
        
        logger.info("Scheduled cleanup job completed");
    }
    
    private void cleanupStudents() {
        logger.info("Cleaning up expired archived students");
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<Student> expiredStudents = studentRepository.findExpiredArchivedStudents(cutoffDate);
        
        if (expiredStudents.isEmpty()) {
            logger.info("No expired archived students found");
            return;
        }
        
        // Log each deletion before removing
        for (Student student : expiredStudents) {
            DeletionLog deletionLog = new DeletionLog();
            deletionLog.setStudentId(student.getId());
            deletionLog.setStudentName(student.getFullName());
            deletionLog.setDeletedAt(LocalDateTime.now());
            deletionLog.setDeletedBy("SYSTEM");
            deletionLog.setReason("Auto-deleted after 30 days in archive");
            deletionLogRepository.save(deletionLog);
            
            logger.info("Logging student deletion: Student ID={}, Name={}", 
                student.getId(), student.getFullName());
        }
        
        // Permanently delete
        studentRepository.deleteAll(expiredStudents);
        
        logger.info("Successfully deleted {} expired archived students", expiredStudents.size());
    }
    
    private void cleanupLessons() {
        logger.info("Cleaning up expired deleted lessons");
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        List<Lesson> expiredLessons = lessonRepository.findExpiredDeletedLessons(cutoffDate);
        
        if (expiredLessons.isEmpty()) {
            logger.info("No expired deleted lessons found");
            return;
        }
        
        // Cascade delete all related records
        for (Lesson lesson : expiredLessons) {
            logger.info("Deleting lesson ID={}, Date={}", lesson.getId(), lesson.getDate());
            
            // Delete related records
            attendanceRepository.deleteByLessonId(lesson.getId());
            homeworkRepository.deleteByLessonId(lesson.getId());
            participationRepository.deleteByLessonId(lesson.getId());
            behavioralIncidentRepository.deleteByLessonId(lesson.getId());
        }
        
        // Permanently delete lessons
        lessonRepository.deleteAll(expiredLessons);
        
        logger.info("Successfully deleted {} expired lessons", expiredLessons.size());
    }
}