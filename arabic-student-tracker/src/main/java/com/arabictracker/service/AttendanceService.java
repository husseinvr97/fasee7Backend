package com.arabictracker.service;


import com.arabictracker.dto.requestDto.BatchUpdateAttendanceRequest;
import com.arabictracker.dto.requestDto.MarkAttendanceRequest;
import com.arabictracker.dto.requestDto.MarkHomeworkRequest;
import com.arabictracker.dto.requestDto.MarkParticipationRequest;
import com.arabictracker.dto.requestDto.ParticipationScoreDto;
import com.arabictracker.dto.requestDto.UpdateAttendanceRequest;
import com.arabictracker.dto.requestDto.UpdateHomeworkRequest;
import com.arabictracker.dto.requestDto.UpdateParticipationRequest;
import com.arabictracker.dto.responseDto.AbsentStudentWarning;
import com.arabictracker.dto.responseDto.AttendanceMarkResponse;
import com.arabictracker.dto.responseDto.AttendanceRecordDto;
import com.arabictracker.dto.responseDto.AttendanceUpdateResponse;
import com.arabictracker.dto.responseDto.AttendanceWarningResponse;
import com.arabictracker.dto.responseDto.BatchAttendanceUpdateResponse;
import com.arabictracker.dto.responseDto.HomeworkMarkResponse;
import com.arabictracker.dto.responseDto.HomeworkUpdateResponse;
import com.arabictracker.dto.responseDto.LessonDetailDto;
import com.arabictracker.dto.responseDto.ParticipationMarkResponse;
import com.arabictracker.dto.responseDto.ParticipationUpdateResponse;
import com.arabictracker.dto.responseDto.StudentAttendanceHistoryResponse;
import com.arabictracker.dto.responseDto.StudentMonthlyAttendanceResponse;
import com.arabictracker.dto.responseDto.StudentWarningDto;
import com.arabictracker.model.*;
import com.arabictracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    
    private final LessonRepository lessonRepository;
    private final LessonAttendanceRepository attendanceRepository;
    private final LessonHomeworkRepository homeworkRepository;
    private final LessonParticipationRepository participationRepository;
    private final StudentRepository studentRepository;
    
    @Transactional
    public AttendanceMarkResponse markAttendance(Long lessonId, MarkAttendanceRequest request) {
        Lesson lesson = lessonRepository.findByIdAndDeletedAtIsNull(lessonId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Lesson not found"
            ));
        
        // Validate: reject archived students
        List<Student> students = studentRepository.findAllById(request.getAttendedStudentIds());
        students.forEach(student -> {
            if (student.getStatus() == Student.StudentStatus.ARCHIVED) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot mark attendance for archived student: " + student.getFullName()
                );
            }
        });
        
        // Get all active students
        List<Student> allActiveStudents = studentRepository.findByStatus(Student.StudentStatus.ACTIVE);
        
        // Clear existing attendance records (update/replace logic)
        attendanceRepository.deleteByLessonId(lessonId);
        
        List<AbsentStudentWarning> warnings = new ArrayList<>();
        int attendedCount = 0;
        int absentCount = 0;
        
        // Mark attendance for all active students
        for (Student student : allActiveStudents) {
            boolean attended = request.getAttendedStudentIds().contains(student.getId());
            
            LessonAttendance attendance = new LessonAttendance();
            attendance.setLesson(lesson);
            attendance.setStudent(student);
            attendance.setAttended(attended);
            
            if (attended) {
                // Reset consecutive absences
                attendance.setConsecutiveAbsences(0);
                attendedCount++;
            } else {
                // Calculate consecutive absences
                int consecutiveAbsences = calculateConsecutiveAbsences(student.getId(), lesson.getDate());
                attendance.setConsecutiveAbsences(consecutiveAbsences);
                absentCount++;
                
                // Check for warnings
                if (consecutiveAbsences >= 2) {
                    AbsentStudentWarning warning = new AbsentStudentWarning();
                    warning.setStudentId(student.getId());
                    warning.setStudentName(student.getFullName());
                    warning.setConsecutiveAbsences(consecutiveAbsences);
                    warning.setWarningTriggered(true);
                    warnings.add(warning);
                }
            }
            
            attendanceRepository.save(attendance);
            
            // Auto-assign participation score = 1 for attended students
            if (attended) {
                LessonParticipation participation = new LessonParticipation();
                participation.setLesson(lesson);
                participation.setStudent(student);
                participation.setScore(1);
                participationRepository.save(participation);
            }
        }
        
        // Mark lesson as attendance marked
        lesson.setAttendanceMarked(true);
        lessonRepository.save(lesson);
        
        AttendanceMarkResponse response = new AttendanceMarkResponse();
        response.setLessonId(lessonId);
        response.setAttendanceMarked(true);
        response.setAttendedCount(attendedCount);
        response.setAbsentCount(absentCount);
        response.setAbsentStudents(warnings);
        
        return response;
    }

    /**
 * Update attendance for a single student (OPTION 1)
 */
@Transactional
public AttendanceUpdateResponse updateSingleStudentAttendance(
        Long lessonId, Long studentId, UpdateAttendanceRequest request) {
    
    // Verify lesson exists and is not deleted
    Lesson lesson = lessonRepository.findByIdAndDeletedAtIsNull(lessonId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Lesson not found"
        ));
    
    // Verify student exists and is active
    Student student = studentRepository.findById(studentId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Student not found"
        ));
    
    if (student.getStatus() == Student.StudentStatus.ARCHIVED) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Cannot update attendance for archived student: " + student.getFullName()
        );
    }
    
    // Find existing attendance record
    LessonAttendance attendance = attendanceRepository
        .findByLessonIdAndStudentId(lessonId, studentId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, 
            "Attendance record not found for this student in this lesson"
        ));
    
    // Store old status for comparison
    boolean wasAttended = attendance.getAttended();
    
    // Update attendance status
    attendance.setAttended(request.getAttended());
    
    // Recalculate consecutive absences if status changed
    if (request.getAttended()) {
        // Student is now marked as attended - reset consecutive absences
        attendance.setConsecutiveAbsences(0);
        
        // Create default participation score if doesn't exist
        if (!participationRepository.findByLessonIdAndStudentId(lessonId, studentId).isPresent()) {
            LessonParticipation participation = new LessonParticipation();
            participation.setLesson(lesson);
            participation.setStudent(student);
            participation.setScore(1);
            participationRepository.save(participation);
        }
    } else {
        // Student is now marked as absent - recalculate consecutive absences
        int consecutiveAbsences = calculateConsecutiveAbsences(studentId, lesson.getDate());
        attendance.setConsecutiveAbsences(consecutiveAbsences);
        
        // Remove participation and homework if they exist
        participationRepository.findByLessonIdAndStudentId(lessonId, studentId)
            .ifPresent(p -> participationRepository.delete(p));
        homeworkRepository.findByLessonIdAndStudentId(lessonId, studentId)
            .ifPresent(h -> homeworkRepository.delete(h));
    }
    
    // Save updated attendance
    attendanceRepository.save(attendance);
    
    // Build response
    AttendanceUpdateResponse response = new AttendanceUpdateResponse();
    response.setLessonId(lessonId);
    response.setStudentId(studentId);
    response.setStudentName(student.getFullName());
    response.setAttended(attendance.getAttended());
    response.setConsecutiveAbsences(attendance.getConsecutiveAbsences());
    
    if (wasAttended != request.getAttended()) {
        response.setMessage(request.getAttended() 
            ? "Student marked as attended" 
            : "Student marked as absent");
    } else {
        response.setMessage("No changes made - status was already " + 
            (request.getAttended() ? "attended" : "absent"));
    }
    
    return response;
}


/**
 * Update attendance for multiple students (OPTION 2)
 */
@Transactional
public BatchAttendanceUpdateResponse updateMultipleStudentsAttendance(
        Long lessonId, BatchUpdateAttendanceRequest request) {
    
    // Verify lesson exists and is not deleted
    Lesson lesson = lessonRepository.findByIdAndDeletedAtIsNull(lessonId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Lesson not found"
        ));
    
    List<AttendanceUpdateResponse> updates = new ArrayList<>();
    
    for (BatchUpdateAttendanceRequest.AttendanceUpdate update : request.getUpdates()) {
        try {
            // Verify student exists and is active
            Student student = studentRepository.findById(update.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Student not found: " + update.getStudentId()
                ));
            
            if (student.getStatus() == Student.StudentStatus.ARCHIVED) {
                AttendanceUpdateResponse errorResponse = new AttendanceUpdateResponse();
                errorResponse.setLessonId(lessonId);
                errorResponse.setStudentId(update.getStudentId());
                errorResponse.setStudentName(student.getFullName());
                errorResponse.setMessage("Skipped: Student is archived");
                updates.add(errorResponse);
                continue;
            }
            
            // Find existing attendance record
            LessonAttendance attendance = attendanceRepository
                .findByLessonIdAndStudentId(lessonId, update.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, 
                    "Attendance record not found for student: " + student.getFullName()
                ));
            
            // Store old status
            boolean wasAttended = attendance.getAttended();
            
            // Update attendance
            attendance.setAttended(update.getAttended());
            
            // Handle consequences of attendance change
            if (update.getAttended()) {
                // Now attended - reset absences
                attendance.setConsecutiveAbsences(0);
                
                // Create default participation if doesn't exist
                if (!participationRepository.findByLessonIdAndStudentId(lessonId, update.getStudentId()).isPresent()) {
                    LessonParticipation participation = new LessonParticipation();
                    participation.setLesson(lesson);
                    participation.setStudent(student);
                    participation.setScore(1);
                    participationRepository.save(participation);
                }
            } else {
                // Now absent - recalculate absences
                int consecutiveAbsences = calculateConsecutiveAbsences(update.getStudentId(), lesson.getDate());
                attendance.setConsecutiveAbsences(consecutiveAbsences);
                
                // Remove participation and homework
                participationRepository.findByLessonIdAndStudentId(lessonId, update.getStudentId())
                    .ifPresent(p -> participationRepository.delete(p));
                homeworkRepository.findByLessonIdAndStudentId(lessonId, update.getStudentId())
                    .ifPresent(h -> homeworkRepository.delete(h));
            }
            
            // Save
            attendanceRepository.save(attendance);
            
            // Build individual response
            AttendanceUpdateResponse updateResponse = new AttendanceUpdateResponse();
            updateResponse.setLessonId(lessonId);
            updateResponse.setStudentId(update.getStudentId());
            updateResponse.setStudentName(student.getFullName());
            updateResponse.setAttended(attendance.getAttended());
            updateResponse.setConsecutiveAbsences(attendance.getConsecutiveAbsences());
            
            if (wasAttended != update.getAttended()) {
                updateResponse.setMessage(update.getAttended() 
                    ? "Updated to attended" 
                    : "Updated to absent");
            } else {
                updateResponse.setMessage("No change needed");
            }
            
            updates.add(updateResponse);
            
        } catch (Exception e) {
            // Log error but continue with other updates
            AttendanceUpdateResponse errorResponse = new AttendanceUpdateResponse();
            errorResponse.setLessonId(lessonId);
            errorResponse.setStudentId(update.getStudentId());
            errorResponse.setMessage("Error: " + e.getMessage());
            updates.add(errorResponse);
        }
    }
    
    // Build batch response
    BatchAttendanceUpdateResponse response = new BatchAttendanceUpdateResponse();
    response.setLessonId(lessonId);
    response.setUpdatedCount(updates.size());
    response.setUpdates(updates);
    response.setMessage("Processed " + updates.size() + " attendance updates");
    
    return response;
}
    
    private int calculateConsecutiveAbsences(Long studentId, LocalDate currentLessonDate) {
        // Get all attendance records for this student before current lesson, ordered by date DESC
        List<LessonAttendance> previousAttendance = attendanceRepository
            .findByStudentAndDateRange(studentId, LocalDate.of(2020, 1, 1), currentLessonDate.minusDays(1))
            .stream()
            .sorted((a, b) -> b.getLesson().getDate().compareTo(a.getLesson().getDate()))
            .collect(Collectors.toList());
        
        int consecutiveAbsences = 1; // Current absence
        
        for (LessonAttendance att : previousAttendance) {
            if (!att.getAttended()) {
                consecutiveAbsences++;
            } else {
                break; // Stop at first attendance
            }
        }
        
        return consecutiveAbsences;
    }
    
    @Transactional
    public HomeworkMarkResponse markHomework(Long lessonId, MarkHomeworkRequest request) {
        Lesson lesson = lessonRepository.findByIdAndDeletedAtIsNull(lessonId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Lesson not found"
            ));
        
        // Validate: only attended students can complete homework
        List<LessonAttendance> attendanceList = attendanceRepository.findByLessonId(lessonId);
        Set<Long> attendedStudentIds = attendanceList.stream()
            .filter(LessonAttendance::getAttended)
            .map(att -> att.getStudent().getId())
            .collect(Collectors.toSet());
        
        for (Long studentId : request.getCompletedStudentIds()) {
            if (!attendedStudentIds.contains(studentId)) {
                Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Student not found"
                    ));
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot mark homework for absent student: " + student.getFullName()
                );
            }
        }
        
        // Clear existing homework records (update/replace logic)
        homeworkRepository.deleteByLessonId(lessonId);
        
        int completedCount = 0;
        int notCompletedCount = 0;
        
        // Mark homework for all attended students
        for (Long studentId : attendedStudentIds) {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Student not found"
                ));
            
            boolean completed = request.getCompletedStudentIds().contains(studentId);
            
            LessonHomework homework = new LessonHomework();
            homework.setLesson(lesson);
            homework.setStudent(student);
            homework.setCompleted(completed);
            homeworkRepository.save(homework);
            
            if (completed) {
                completedCount++;
            } else {
                notCompletedCount++;
            }
        }
        
        // Mark lesson as homework marked
        lesson.setHomeworkMarked(true);
        lessonRepository.save(lesson);
        
        HomeworkMarkResponse response = new HomeworkMarkResponse();
        response.setLessonId(lessonId);
        response.setHomeworkMarked(true);
        response.setCompletedCount(completedCount);
        response.setNotCompletedCount(notCompletedCount);
        
        // Warning if lesson marked as no homework
        if (!lesson.getHasHomework()) {
            response.setWarning("Lesson marked as no homework");
        }
        
        return response;
    }
    
    @Transactional
    public ParticipationMarkResponse markParticipation(Long lessonId, MarkParticipationRequest request) {
        Lesson lesson = lessonRepository.findByIdAndDeletedAtIsNull(lessonId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Lesson not found"
            ));
        
        // Validate: only attended students can have participation scored
        List<LessonAttendance> attendanceList = attendanceRepository.findByLessonId(lessonId);
        Set<Long> attendedStudentIds = attendanceList.stream()
            .filter(LessonAttendance::getAttended)
            .map(att -> att.getStudent().getId())
            .collect(Collectors.toSet());
        
        for (ParticipationScoreDto scoreDto : request.getParticipationScores()) {
            if (!attendedStudentIds.contains(scoreDto.getStudentId())) {
                Student student = studentRepository.findById(scoreDto.getStudentId())
                    .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Student not found"
                    ));
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot mark participation for absent student: " + student.getFullName()
                );
            }
        }
        
        // Update participation scores (replace existing)
        for (ParticipationScoreDto scoreDto : request.getParticipationScores()) {
            Student student = studentRepository.findById(scoreDto.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Student not found"
                ));
            
            LessonParticipation participation = participationRepository
                .findByLessonIdAndStudentId(lessonId, scoreDto.getStudentId())
                .orElse(new LessonParticipation());
            
            participation.setLesson(lesson);
            participation.setStudent(student);
            participation.setScore(scoreDto.getScore());
            participationRepository.save(participation);
        }
        
        // Mark lesson as participation marked
        lesson.setParticipationMarked(true);
        lessonRepository.save(lesson);
        
        ParticipationMarkResponse response = new ParticipationMarkResponse();
        response.setLessonId(lessonId);
        response.setParticipationMarked(true);
        response.setStudentsScored(request.getParticipationScores().size());
        
        return response;
    }

    /**
 * Update homework completion for a single student
 */
@Transactional
public HomeworkUpdateResponse updateSingleStudentHomework(
        Long lessonId, Long studentId, UpdateHomeworkRequest request) {
    
    // Verify lesson exists and is not deleted
    Lesson lesson = lessonRepository.findByIdAndDeletedAtIsNull(lessonId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Lesson not found"
        ));
    
    // Check if lesson has homework
    if (!lesson.getHasHomework()) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "This lesson is marked as having no homework"
        );
    }
    
    // Verify student exists and is active
    Student student = studentRepository.findById(studentId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Student not found"
        ));
    
    if (student.getStatus() == Student.StudentStatus.ARCHIVED) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Cannot update homework for archived student: " + student.getFullName()
        );
    }
    
    // Verify student attended the lesson
    LessonAttendance attendance = attendanceRepository
        .findByLessonIdAndStudentId(lessonId, studentId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Attendance record not found for this student"
        ));
    
    if (!attendance.getAttended()) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Cannot mark homework for absent student: " + student.getFullName()
        );
    }
    
    // Find or create homework record
    LessonHomework homework = homeworkRepository
        .findByLessonIdAndStudentId(lessonId, studentId)
        .orElse(new LessonHomework());
    
    // Store old status for comparison
    Boolean wasCompleted = homework.getId() != null ? homework.getCompleted() : null;
    
    // Update homework
    homework.setLesson(lesson);
    homework.setStudent(student);
    homework.setCompleted(request.getCompleted());
    
    // Save
    homeworkRepository.save(homework);
    
    // Build response
    HomeworkUpdateResponse response = new HomeworkUpdateResponse();
    response.setLessonId(lessonId);
    response.setStudentId(studentId);
    response.setStudentName(student.getFullName());
    response.setCompleted(homework.getCompleted());
    
    if (wasCompleted == null) {
        response.setMessage("Homework status created: " + 
            (request.getCompleted() ? "completed" : "not completed"));
    } else if (!wasCompleted.equals(request.getCompleted())) {
        response.setMessage("Homework updated to: " + 
            (request.getCompleted() ? "completed" : "not completed"));
    } else {
        response.setMessage("No change - homework was already " + 
            (request.getCompleted() ? "completed" : "not completed"));
    }
    
    return response;
}


/**
 * Update participation score for a single student
 */
@Transactional
public ParticipationUpdateResponse updateSingleStudentParticipation(
        Long lessonId, Long studentId, UpdateParticipationRequest request) {
    
    // Verify lesson exists and is not deleted
    Lesson lesson = lessonRepository.findByIdAndDeletedAtIsNull(lessonId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Lesson not found"
        ));
    
    // Verify student exists and is active
    Student student = studentRepository.findById(studentId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Student not found"
        ));
    
    if (student.getStatus() == Student.StudentStatus.ARCHIVED) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Cannot update participation for archived student: " + student.getFullName()
        );
    }
    
    // Verify student attended the lesson
    LessonAttendance attendance = attendanceRepository
        .findByLessonIdAndStudentId(lessonId, studentId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Attendance record not found for this student"
        ));
    
    if (!attendance.getAttended()) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Cannot mark participation for absent student: " + student.getFullName()
        );
    }
    
    // Find or create participation record
    LessonParticipation participation = participationRepository
        .findByLessonIdAndStudentId(lessonId, studentId)
        .orElse(new LessonParticipation());
    
    // Store old score for comparison
    Integer oldScore = participation.getId() != null ? participation.getScore() : null;
    
    // Update participation
    participation.setLesson(lesson);
    participation.setStudent(student);
    participation.setScore(request.getScore());
    
    // Save
    participationRepository.save(participation);
    
    // Build response
    ParticipationUpdateResponse response = new ParticipationUpdateResponse();
    response.setLessonId(lessonId);
    response.setStudentId(studentId);
    response.setStudentName(student.getFullName());
    response.setScore(participation.getScore());
    
    if (oldScore == null) {
        response.setMessage("Participation score created: " + request.getScore());
    } else if (!oldScore.equals(request.getScore())) {
        response.setMessage("Participation score updated from " + oldScore + " to " + request.getScore());
    } else {
        response.setMessage("No change - score was already " + request.getScore());
    }
    
    return response;
}
    
    public StudentAttendanceHistoryResponse getStudentAttendanceHistory(
            Long studentId, LocalDate startDate, LocalDate endDate) {
        
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Student not found"
            ));
        
        List<LessonAttendance> attendanceList = attendanceRepository
            .findByStudentAndDateRange(studentId, startDate, endDate);
        
        int totalLessons = attendanceList.size();
        long attended = attendanceList.stream().filter(LessonAttendance::getAttended).count();
        int absent = totalLessons - (int) attended;
        double percentage = totalLessons > 0 ? (attended * 100.0 / totalLessons) : 0.0;
        
        // Get current consecutive absences
        int consecutiveAbsences = attendanceList.isEmpty() ? 0 : 
            attendanceList.get(attendanceList.size() - 1).getConsecutiveAbsences();
        
        List<AttendanceRecordDto> records = attendanceList.stream()
            .map(att -> {
                AttendanceRecordDto dto = new AttendanceRecordDto();
                dto.setLessonId(att.getLesson().getId());
                dto.setDate(att.getLesson().getDate());
                dto.setAttended(att.getAttended());
                return dto;
            })
            .collect(Collectors.toList());
        
        StudentAttendanceHistoryResponse response = new StudentAttendanceHistoryResponse();
        response.setStudentId(studentId);
        response.setStudentName(student.getFullName());
        response.setTotalLessons(totalLessons);
        response.setAttended((int) attended);
        response.setAbsent(absent);
        response.setAttendancePercentage(percentage);
        response.setConsecutiveAbsences(consecutiveAbsences);
        response.setAttendanceRecords(records);
        
        return response;
    }
    
    public StudentMonthlyAttendanceResponse getStudentMonthlyAttendance(
            Long studentId, int year, int month) {
        
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Student not found"
            ));
        
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        List<LessonAttendance> attendanceList = attendanceRepository
            .findByStudentAndDateRange(studentId, startDate, endDate);
        
        int totalLessons = attendanceList.size();
        long attended = attendanceList.stream().filter(LessonAttendance::getAttended).count();
        int absent = totalLessons - (int) attended;
        double percentage = totalLessons > 0 ? (attended * 100.0 / totalLessons) : 0.0;
        
        List<LessonDetailDto> lessonDetails = attendanceList.stream()
            .map(att -> {
                LessonDetailDto dto = new LessonDetailDto();
                dto.setLessonId(att.getLesson().getId());
                dto.setDate(att.getLesson().getDate());
                dto.setAttended(att.getAttended());
                
                if (att.getAttended()) {
                    // Get homework status
                    homeworkRepository.findByLessonIdAndStudentId(att.getLesson().getId(), studentId)
                        .ifPresent(hw -> dto.setHomeworkCompleted(hw.getCompleted()));
                    
                    // Get participation score
                    participationRepository.findByLessonIdAndStudentId(att.getLesson().getId(), studentId)
                        .ifPresent(p -> dto.setParticipationScore(p.getScore()));
                } else {
                    dto.setConsecutiveAbsences(att.getConsecutiveAbsences());
                }
                
                return dto;
            })
            .collect(Collectors.toList());
        
        StudentMonthlyAttendanceResponse response = new StudentMonthlyAttendanceResponse();
        response.setStudentId(studentId);
        response.setStudentName(student.getFullName());
        response.setYear(year);
        response.setMonth(month);
        response.setTotalLessons(totalLessons);
        response.setAttended((int) attended);
        response.setAbsent(absent);
        response.setAttendancePercentage(percentage);
        response.setLessonDetails(lessonDetails);
        
        return response;
    }
    
    public AttendanceWarningResponse getStudentsNeedingWarnings() {
        List<LessonAttendance> warningAttendance = attendanceRepository.findStudentsNeedingWarnings();
        
        List<StudentWarningDto> warnings = warningAttendance.stream()
            .map(att -> {
                StudentWarningDto dto = new StudentWarningDto();
                dto.setStudentId(att.getStudent().getId());
                dto.setStudentName(att.getStudent().getFullName());
                dto.setConsecutiveAbsences(att.getConsecutiveAbsences());
                dto.setWarningType(att.getConsecutiveAbsences() >= 3 ? "DELETION_TRIGGER" : "TWO_ABSENCES");
                dto.setParentPhone(att.getStudent().getParent().getPhone());
                dto.setParentPreferredContact(att.getStudent().getParent().getPreferredContactMethod().name());
                return dto;
            })
            .collect(Collectors.toList());
        
        AttendanceWarningResponse response = new AttendanceWarningResponse();
        response.setWarnings(warnings);
        return response;
    }
}