package com.arabictracker.service;

import com.arabictracker.dto.requestDto.CreateLessonRequest;
import com.arabictracker.dto.requestDto.UpdateLessonRequest;
import com.arabictracker.dto.responseDto.AbsentStudentDto;
import com.arabictracker.dto.responseDto.AttendedStudentDto;
import com.arabictracker.dto.responseDto.LessonDetailResponse;
import com.arabictracker.dto.responseDto.LessonResponse;
import com.arabictracker.model.*;
import com.arabictracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {
    
    private final LessonRepository lessonRepository;
    private final LessonAttendanceRepository attendanceRepository;
    private final LessonHomeworkRepository homeworkRepository;
    private final LessonParticipationRepository participationRepository;
    private final StudentRepository studentRepository;
    
    @Transactional
    public LessonResponse createLesson(CreateLessonRequest request) {
        // Check if lesson already exists for this date
        lessonRepository.findByDateAndDeletedAtIsNull(request.getDate())
            .ifPresent(lesson -> {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Lesson already exists for date: " + request.getDate()
                );
            });
        
        Lesson lesson = new Lesson();
        lesson.setDate(request.getDate());
        lesson.setTopicsFreeText(request.getTopicsFreeText());
        lesson.setCategoryTags(request.getCategoryTags());
        lesson.setHasHomework(request.getHasHomework());
        
        Lesson saved = lessonRepository.save(lesson);
        return mapToLessonResponse(saved);
    }
    
    public List<LessonResponse> getAllLessons(LocalDate startDate, LocalDate endDate) {
        List<Lesson> lessons;
        
        if (startDate != null && endDate != null) {
            lessons = lessonRepository.findByDateBetweenAndDeletedAtIsNull(startDate, endDate);
        } else {
            lessons = lessonRepository.findByDeletedAtIsNull();
        }
        
        return lessons.stream()
            .map(this::mapToLessonResponse)
            .collect(Collectors.toList());
    }
    
    public LessonDetailResponse getLessonById(Long lessonId) {
        Lesson lesson = lessonRepository.findByIdAndDeletedAtIsNull(lessonId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Lesson not found"
            ));
        
        return mapToLessonDetailResponse(lesson);
    }
    
    @Transactional
    public LessonResponse updateLesson(Long lessonId, UpdateLessonRequest request) {
        Lesson lesson = lessonRepository.findByIdAndDeletedAtIsNull(lessonId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Lesson not found"
            ));
        
        // Check if changing date conflicts with another lesson
        if (!lesson.getDate().equals(request.getDate())) {
            lessonRepository.findByDateAndDeletedAtIsNull(request.getDate())
                .ifPresent(existingLesson -> {
                    throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Another lesson already exists for date: " + request.getDate()
                    );
                });
        }
        
        lesson.setDate(request.getDate());
        lesson.setTopicsFreeText(request.getTopicsFreeText());
        lesson.setCategoryTags(request.getCategoryTags());
        lesson.setHasHomework(request.getHasHomework());
        
        Lesson updated = lessonRepository.save(lesson);
        return mapToLessonResponse(updated);
    }
    
    @Transactional
    public void deleteLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findByIdAndDeletedAtIsNull(lessonId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Lesson not found"
            ));
        
        lesson.setDeletedAt(java.time.LocalDateTime.now());
        lessonRepository.save(lesson);
    }

    // Add these methods to your existing LessonService class

/**
 * Get all soft-deleted lessons
 */
public List<LessonResponse> getDeletedLessons() {
    List<Lesson> deletedLessons = lessonRepository.findByDeletedAtIsNotNull();
    
    return deletedLessons.stream()
        .map(this::mapToLessonResponse)
        .collect(Collectors.toList());
}

/**
 * Restore a soft-deleted lesson
 */
@Transactional
public LessonResponse restoreLesson(Long lessonId) {
    Lesson lesson = lessonRepository.findByIdAndDeletedAtIsNotNull(lessonId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Deleted lesson not found with ID: " + lessonId
        ));
    
    // Check if restoring this lesson would conflict with an existing active lesson on the same date
    lessonRepository.findByDateAndDeletedAtIsNull(lesson.getDate())
        .ifPresent(existingLesson -> {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Cannot restore lesson. Another active lesson already exists for date: " + lesson.getDate()
            );
        });
    
    // Restore the lesson by clearing the deletedAt timestamp
    lesson.setDeletedAt(null);
    Lesson restored = lessonRepository.save(lesson);
    
    return mapToLessonResponse(restored);
}

/**
 * Permanently delete a lesson (hard delete)
 */
@Transactional
public void permanentlyDeleteLesson(Long lessonId) {
    Lesson lesson = lessonRepository.findByIdAndDeletedAtIsNotNull(lessonId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Deleted lesson not found with ID: " + lessonId
        ));
    
    // Permanently delete associated data first (to maintain referential integrity)
    // Delete attendance records
    attendanceRepository.deleteByLessonId(lessonId);
    
    // Delete homework records
    homeworkRepository.deleteByLessonId(lessonId);
    
    // Delete participation records
    participationRepository.deleteByLessonId(lessonId);
    
    // Note: If you have behavioral incidents tied to lessons, delete those too
    // behavioralIncidentRepository.deleteByLessonId(lessonId);
    
    // Finally, permanently delete the lesson itself
    lessonRepository.delete(lesson);
}
    
    private LessonResponse mapToLessonResponse(Lesson lesson) {
        LessonResponse response = new LessonResponse();
        response.setId(lesson.getId());
        response.setDate(lesson.getDate());
        response.setTopicsFreeText(lesson.getTopicsFreeText());
        response.setCategoryTags(lesson.getCategoryTags());
        response.setHasHomework(lesson.getHasHomework());
        response.setAttendanceMarked(lesson.getAttendanceMarked());
        response.setHomeworkMarked(lesson.getHomeworkMarked());
        response.setParticipationMarked(lesson.getParticipationMarked());
        
        // Calculate attendance statistics
        List<LessonAttendance> attendanceList = attendanceRepository.findByLessonId(lesson.getId());
        long attendedCount = attendanceList.stream().filter(LessonAttendance::getAttended).count();
        response.setAttendanceCount((int) attendedCount);
        
        // Total active students
        long totalActive = studentRepository.findByStatus(Student.StudentStatus.ACTIVE).size();
        response.setTotalStudents((int) totalActive);
        
        response.setCreatedAt(lesson.getCreatedAt());
        response.setUpdatedAt(lesson.getUpdatedAt());
        
        return response;
    }
    
    private LessonDetailResponse mapToLessonDetailResponse(Lesson lesson) {
        LessonDetailResponse response = new LessonDetailResponse();
        response.setId(lesson.getId());
        response.setDate(lesson.getDate());
        response.setTopicsFreeText(lesson.getTopicsFreeText());
        response.setCategoryTags(lesson.getCategoryTags());
        response.setHasHomework(lesson.getHasHomework());
        response.setAttendanceMarked(lesson.getAttendanceMarked());
        response.setHomeworkMarked(lesson.getHomeworkMarked());
        response.setParticipationMarked(lesson.getParticipationMarked());
        response.setCreatedAt(lesson.getCreatedAt());
        response.setUpdatedAt(lesson.getUpdatedAt());
        
        // Get attendance records
        List<LessonAttendance> attendanceList = attendanceRepository.findByLessonId(lesson.getId());
        
        // Attended students
        List<AttendedStudentDto> attendedStudents = attendanceList.stream()
            .filter(LessonAttendance::getAttended)
            .map(att -> {
                AttendedStudentDto dto = new AttendedStudentDto();
                dto.setStudentId(att.getStudent().getId());
                dto.setStudentName(att.getStudent().getFullName());
                
                // Get homework status
                homeworkRepository.findByLessonIdAndStudentId(lesson.getId(), att.getStudent().getId())
                    .ifPresent(hw -> dto.setHomeworkCompleted(hw.getCompleted()));
                
                // Get participation score
                participationRepository.findByLessonIdAndStudentId(lesson.getId(), att.getStudent().getId())
                    .ifPresent(p -> dto.setParticipationScore(p.getScore()));
                
                return dto;
            })
            .collect(Collectors.toList());
        response.setAttendedStudents(attendedStudents);
        
        // Absent students
        List<AbsentStudentDto> absentStudents = attendanceList.stream()
            .filter(att -> !att.getAttended())
            .map(att -> {
                AbsentStudentDto dto = new AbsentStudentDto();
                dto.setStudentId(att.getStudent().getId());
                dto.setStudentName(att.getStudent().getFullName());
                dto.setConsecutiveAbsences(att.getConsecutiveAbsences());
                return dto;
            })
            .collect(Collectors.toList());
        response.setAbsentStudents(absentStudents);
        
        return response;
    }
}