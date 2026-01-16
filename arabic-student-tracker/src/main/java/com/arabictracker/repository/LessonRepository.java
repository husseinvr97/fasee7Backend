package com.arabictracker.repository;

import com.arabictracker.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    
    Optional<Lesson> findByDateAndDeletedAtIsNull(LocalDate date);
    
    List<Lesson> findByDeletedAtIsNull();
    
    List<Lesson> findByDateBetweenAndDeletedAtIsNull(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT l FROM Lesson l WHERE l.deletedAt < :cutoffDate")
    List<Lesson> findExpiredDeletedLessons(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    Optional<Lesson> findByIdAndDeletedAtIsNull(Long id);
}