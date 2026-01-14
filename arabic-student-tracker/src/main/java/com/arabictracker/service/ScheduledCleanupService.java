package com.arabictracker.service;

import com.arabictracker.model.DeletionLog;
import com.arabictracker.model.Student;
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
    
    /**
     * Runs daily at 2 AM to permanently delete students archived for 30+ days
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredArchivedStudents() {
        logger.info("Starting scheduled cleanup of expired archived students");
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<Student> expiredStudents = studentRepository.findExpiredArchivedStudents(cutoffDate);
        
        if (expiredStudents.isEmpty()) {
            logger.info("No expired archived students found");
            return;
        }
        
        // Log each deletion before removing
        for (Student student : expiredStudents) {
            DeletionLog log = new DeletionLog();
            log.setStudentId(student.getId());
            log.setStudentName(student.getFullName());
            log.setDeletedAt(LocalDateTime.now());
            log.setDeletedBy("SYSTEM");
            log.setReason("Auto-deleted after 30 days in archive");
            deletionLogRepository.save(log);
            
            logger.info("Logging deletion: Student ID={}, Name={}", 
                student.getId(), student.getFullName());
        }
        
        // Permanently delete
        studentRepository.deleteAll(expiredStudents);
        
        logger.info("Successfully deleted {} expired archived students", expiredStudents.size());
    }
}