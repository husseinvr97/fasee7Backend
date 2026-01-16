package com.arabictracker.repository;

import com.arabictracker.model.LessonHomework;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonHomeworkRepository extends JpaRepository<LessonHomework, Long> {
    
    List<LessonHomework> findByLessonId(Long lessonId);
    
    Optional<LessonHomework> findByLessonIdAndStudentId(Long lessonId, Long studentId);
    
    void deleteByLessonId(Long lessonId);
}