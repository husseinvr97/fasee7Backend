package com.arabictracker.repository;

import com.arabictracker.model.Student;
import com.arabictracker.model.Student.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    List<Student> findByStatus(StudentStatus status);
    
    @Query("SELECT s FROM Student s WHERE s.status != 'ARCHIVED' " +
           "AND (LOWER(s.searchName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(s.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(s.fatherName) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Student> searchActiveStudents(@Param("search") String search);
    
    @Query("SELECT s FROM Student s WHERE s.status = 'ARCHIVED' " +
           "AND s.archivedDate < :cutoffDate")
    List<Student> findExpiredArchivedStudents(@Param("cutoffDate") LocalDateTime cutoffDate);
}