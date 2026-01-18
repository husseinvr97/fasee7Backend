package com.arabictracker.controller;

import com.arabictracker.dto.requestDto.BatchUpdateAttendanceRequest;
import com.arabictracker.dto.requestDto.CreateBehavioralIncidentRequest;
import com.arabictracker.dto.requestDto.CreateLessonRequest;
import com.arabictracker.dto.requestDto.MarkAttendanceRequest;
import com.arabictracker.dto.requestDto.MarkHomeworkRequest;
import com.arabictracker.dto.requestDto.MarkParticipationRequest;
import com.arabictracker.dto.requestDto.UpdateAttendanceRequest;
import com.arabictracker.dto.requestDto.UpdateHomeworkRequest;
import com.arabictracker.dto.requestDto.UpdateLessonRequest;
import com.arabictracker.dto.requestDto.UpdateParticipationRequest;
import com.arabictracker.dto.responseDto.AttendanceMarkResponse;
import com.arabictracker.dto.responseDto.AttendanceUpdateResponse;
import com.arabictracker.dto.responseDto.BatchAttendanceUpdateResponse;
import com.arabictracker.dto.responseDto.BehavioralIncidentResponse;
import com.arabictracker.dto.responseDto.HomeworkMarkResponse;
import com.arabictracker.dto.responseDto.HomeworkUpdateResponse;
import com.arabictracker.dto.responseDto.LessonDetailResponse;
import com.arabictracker.dto.responseDto.LessonResponse;
import com.arabictracker.dto.responseDto.ParticipationMarkResponse;
import com.arabictracker.dto.responseDto.ParticipationUpdateResponse;
import com.arabictracker.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {
    
    private final LessonService lessonService;
    private final AttendanceService attendanceService;
    private final BehavioralService behavioralService;
    
    @PostMapping
    public ResponseEntity<LessonResponse> createLesson(
            @Valid @RequestBody CreateLessonRequest request) {
        LessonResponse response = lessonService.createLesson(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<List<LessonResponse>> getAllLessons(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<LessonResponse> lessons = lessonService.getAllLessons(startDate, endDate);
        return ResponseEntity.ok(lessons);
    }
    
    @GetMapping("/{lessonId}")
    public ResponseEntity<LessonDetailResponse> getLessonById(@PathVariable Long lessonId) {
        LessonDetailResponse lesson = lessonService.getLessonById(lessonId);
        return ResponseEntity.ok(lesson);
    }
    
    @PutMapping("/{lessonId}")
    public ResponseEntity<LessonResponse> updateLesson(
            @PathVariable Long lessonId,
            @Valid @RequestBody UpdateLessonRequest request) {
        LessonResponse updated = lessonService.updateLesson(lessonId, request);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{lessonId}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long lessonId) {
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{lessonId}/attendance")
    public ResponseEntity<AttendanceMarkResponse> markAttendance(
            @PathVariable Long lessonId,
            @Valid @RequestBody MarkAttendanceRequest request) {
        AttendanceMarkResponse response = attendanceService.markAttendance(lessonId, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{lessonId}/homework")
    public ResponseEntity<HomeworkMarkResponse> markHomework(
            @PathVariable Long lessonId,
            @Valid @RequestBody MarkHomeworkRequest request) {
        HomeworkMarkResponse response = attendanceService.markHomework(lessonId, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{lessonId}/participation")
    public ResponseEntity<ParticipationMarkResponse> markParticipation(
            @PathVariable Long lessonId,
            @Valid @RequestBody MarkParticipationRequest request) {
        ParticipationMarkResponse response = attendanceService.markParticipation(lessonId, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{lessonId}/behavioral-incidents")
    public ResponseEntity<BehavioralIncidentResponse> createBehavioralIncident(
            @PathVariable Long lessonId,
            @Valid @RequestBody CreateBehavioralIncidentRequest request) {
        BehavioralIncidentResponse response = behavioralService.createBehavioralIncident(lessonId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Add these endpoints to your existing LessonController class

/**
 * GET /api/lessons/deleted - Get all soft-deleted lessons
 */
@GetMapping("/deleted")
public ResponseEntity<List<LessonResponse>> getDeletedLessons() {
    List<LessonResponse> deletedLessons = lessonService.getDeletedLessons();
    return ResponseEntity.ok(deletedLessons);
}

@GetMapping("/{lessonId}/behavioral-incidents")
public ResponseEntity<List<BehavioralIncidentResponse>> getLessonBehavioralIncidents(
        @PathVariable Long lessonId) {
    
    List<BehavioralIncidentResponse> incidents = behavioralService.getLessonIncidents(lessonId);
    return ResponseEntity.ok(incidents);
}

/**
 * POST /api/lessons/{lessonId}/restore - Restore a soft-deleted lesson
 */
@PostMapping("/{lessonId}/restore")
public ResponseEntity<LessonResponse> restoreLesson(@PathVariable Long lessonId) {
    LessonResponse restored = lessonService.restoreLesson(lessonId);
    return ResponseEntity.ok(restored);
}

/**
 * DELETE /api/lessons/{lessonId}/permanent - Permanently delete a lesson
 */
@DeleteMapping("/{lessonId}/permanent")
public ResponseEntity<Void> permanentlyDeleteLesson(@PathVariable Long lessonId) {
    lessonService.permanentlyDeleteLesson(lessonId);
    return ResponseEntity.noContent().build();
}

/**
 * OPTION 1: Update single student attendance
 * PATCH /api/lessons/{lessonId}/attendance/{studentId}
 */
@PatchMapping("/{lessonId}/attendance/{studentId}")
public ResponseEntity<AttendanceUpdateResponse> updateSingleStudentAttendance(
        @PathVariable Long lessonId,
        @PathVariable Long studentId,
        @Valid @RequestBody UpdateAttendanceRequest request) {
    
    AttendanceUpdateResponse response = attendanceService.updateSingleStudentAttendance(
        lessonId, studentId, request
    );
    return ResponseEntity.ok(response);
}

/**
 * OPTION 2: Update multiple students attendance (batch update)
 * PATCH /api/lessons/{lessonId}/attendance
 */
@PatchMapping("/{lessonId}/attendance")
public ResponseEntity<BatchAttendanceUpdateResponse> updateMultipleStudentsAttendance(
        @PathVariable Long lessonId,
        @Valid @RequestBody BatchUpdateAttendanceRequest request) {
    
    BatchAttendanceUpdateResponse response = attendanceService.updateMultipleStudentsAttendance(
        lessonId, request
    );
    return ResponseEntity.ok(response);
}

/**
 * Update single student homework completion
 * PATCH /api/lessons/{lessonId}/homework/{studentId}
 */
@PatchMapping("/{lessonId}/homework/{studentId}")
public ResponseEntity<HomeworkUpdateResponse> updateSingleStudentHomework(
        @PathVariable Long lessonId,
        @PathVariable Long studentId,
        @Valid @RequestBody UpdateHomeworkRequest request) {
    
    HomeworkUpdateResponse response = attendanceService.updateSingleStudentHomework(
        lessonId, studentId, request
    );
    return ResponseEntity.ok(response);
}

/**
 * Update single student participation score
 * PATCH /api/lessons/{lessonId}/participation/{studentId}
 */
@PatchMapping("/{lessonId}/participation/{studentId}")
public ResponseEntity<ParticipationUpdateResponse> updateSingleStudentParticipation(
        @PathVariable Long lessonId,
        @PathVariable Long studentId,
        @Valid @RequestBody UpdateParticipationRequest request) {
    
    ParticipationUpdateResponse response = attendanceService.updateSingleStudentParticipation(
        lessonId, studentId, request
    );
    return ResponseEntity.ok(response);
}
}
