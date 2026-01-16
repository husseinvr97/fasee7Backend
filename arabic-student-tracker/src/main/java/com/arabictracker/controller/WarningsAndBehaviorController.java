package com.arabictracker.controller;

import com.arabictracker.dto.responseDto.AttendanceWarningResponse;
import com.arabictracker.dto.responseDto.BehavioralSummaryResponse;
import com.arabictracker.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WarningsAndBehaviorController {
    
    private final AttendanceService attendanceService;
    private final BehavioralService behavioralService;
    
    @GetMapping("/attendance/warnings")
    public ResponseEntity<AttendanceWarningResponse> getStudentsNeedingWarnings() {
        AttendanceWarningResponse response = attendanceService.getStudentsNeedingWarnings();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/students/{studentId}/behavioral-summary")
    public ResponseEntity<BehavioralSummaryResponse> getStudentBehavioralSummary(
            @PathVariable Long studentId,
            @RequestParam String month) {
        BehavioralSummaryResponse response = behavioralService.getStudentBehavioralSummary(studentId, month);
        return ResponseEntity.ok(response);
    }
}