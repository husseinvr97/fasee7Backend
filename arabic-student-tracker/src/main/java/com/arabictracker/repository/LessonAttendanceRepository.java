package com.arabictracker.repository;

import com.arabictracker.model.LessonAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LessonAttendanceRepository extends JpaRepository<LessonAttendance, Long> {
    
    List<LessonAttendance> findByLessonId(Long lessonId);
    
    Optional<LessonAttendance> findByLessonIdAndStudentId(Long lessonId, Long studentId);
    
    @Query("SELECT la FROM LessonAttendance la " +
           "WHERE la.student.id = :studentId " +
           "AND la.lesson.date BETWEEN :startDate AND :endDate " +
           "ORDER BY la.lesson.date")
    List<LessonAttendance> findByStudentAndDateRange(
        @Param("studentId") Long studentId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT la FROM LessonAttendance la " +
           "WHERE la.student.status = 'ACTIVE' " +
           "AND la.consecutiveAbsences >= 2 " +
           "ORDER BY la.consecutiveAbsences DESC")
    List<LessonAttendance> findStudentsNeedingWarnings();
    
    void deleteByLessonId(Long lessonId);
}