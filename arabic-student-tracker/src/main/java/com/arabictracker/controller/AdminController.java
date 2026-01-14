package com.arabictracker.controller;

import com.arabictracker.dto.responseDto.DeletionResponse;
import com.arabictracker.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/students")
@RequiredArgsConstructor
public class AdminController {
    
    private final StudentService studentService;
    
    /**
     * Permanently delete student - ADMIN ONLY
     * Bypasses 30-day archive rule
     * Use for emergency cleanup or testing
     */
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<DeletionResponse> permanentlyDeleteStudent(
            @PathVariable Long id,
            @RequestHeader("X-Admin-User") String adminUser) {
        
        // TODO: Add proper admin authentication/authorization
        // For now, accepting admin username from header
        
        DeletionResponse response = studentService.permanentlyDeleteStudent(id, adminUser);
        return ResponseEntity.ok(response);
    }
}