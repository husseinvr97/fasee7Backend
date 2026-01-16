package com.arabictracker.controller;

import com.arabictracker.dto.responseDto.StudentAttendanceHistoryResponse;
import com.arabictracker.dto.responseDto.StudentMonthlyAttendanceResponse;
import com.arabictracker.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentAttendanceController {
    
    private final AttendanceService attendanceService;
    
    @GetMapping("/{studentId}/attendance")
    public ResponseEntity<StudentAttendanceHistoryResponse> getStudentAttendanceHistory(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        StudentAttendanceHistoryResponse response = attendanceService.getStudentAttendanceHistory(
            studentId, startDate, endDate
        );
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{studentId}/attendance/monthly")
    public ResponseEntity<StudentMonthlyAttendanceResponse> getStudentMonthlyAttendance(
            @PathVariable Long studentId,
            @RequestParam int year,
            @RequestParam int month) {
        StudentMonthlyAttendanceResponse response = attendanceService.getStudentMonthlyAttendance(
            studentId, year, month
        );
        return ResponseEntity.ok(response);
    }
}