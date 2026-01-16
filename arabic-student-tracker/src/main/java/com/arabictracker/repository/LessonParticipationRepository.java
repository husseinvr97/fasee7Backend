package com.arabictracker.repository;

import com.arabictracker.model.LessonParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonParticipationRepository extends JpaRepository<LessonParticipation, Long> {
    
    List<LessonParticipation> findByLessonId(Long lessonId);
    
    Optional<LessonParticipation> findByLessonIdAndStudentId(Long lessonId, Long studentId);
    
    @Query("SELECT AVG(lp.score) FROM LessonParticipation lp " +
           "WHERE lp.student.id = :studentId")
    Double calculateAverageScore(@Param("studentId") Long studentId);
    
    void deleteByLessonId(Long lessonId);
}