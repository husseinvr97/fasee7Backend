package com.arabictracker.repository;

import com.arabictracker.model.BehavioralIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BehavioralIncidentRepository extends JpaRepository<BehavioralIncident, Long> {
    
    List<BehavioralIncident> findByLessonId(Long lessonId);
    
    @Query("SELECT COUNT(bi) FROM BehavioralIncident bi " +
           "WHERE bi.student.id = :studentId " +
           "AND YEAR(bi.createdAt) = :year " +
           "AND MONTH(bi.createdAt) = :month")
    Long countByStudentAndMonth(
        @Param("studentId") Long studentId,
        @Param("year") int year,
        @Param("month") int month
    );
    
    @Query("SELECT bi FROM BehavioralIncident bi " +
           "WHERE bi.student.id = :studentId " +
           "AND YEAR(bi.createdAt) = :year " +
           "AND MONTH(bi.createdAt) = :month " +
           "ORDER BY bi.createdAt DESC")
    List<BehavioralIncident> findByStudentAndMonth(
        @Param("studentId") Long studentId,
        @Param("year") int year,
        @Param("month") int month
    );
    
    void deleteByLessonId(Long lessonId);
}