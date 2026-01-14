package com.arabictracker.controller;

import com.arabictracker.dto.*;
import com.arabictracker.dto.requestDto.CreateStudentRequest;
import com.arabictracker.dto.requestDto.UpdateStudentRequest;
import com.arabictracker.dto.responseDto.StudentResponse;
import com.arabictracker.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {
    
    private final StudentService studentService;
    
    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(
            @Valid @RequestBody CreateStudentRequest request) {
        StudentResponse response = studentService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<List<StudentResponse>> getActiveStudents(
            @RequestParam(required = false) String search) {
        List<StudentResponse> students = search != null && !search.isEmpty()
            ? studentService.searchStudents(search)
            : studentService.getActiveStudents();
        return ResponseEntity.ok(students);
    }
    
    @GetMapping("/archived")
    public ResponseEntity<List<StudentResponse>> getArchivedStudents() {
        List<StudentResponse> students = studentService.getArchivedStudents();
        return ResponseEntity.ok(students);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<StudentResponse>> getActiveStudents() {
        List<StudentResponse> students = studentService.getActiveStudents();
        return ResponseEntity.ok(students);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable Long id) {
        StudentResponse student = studentService.getStudentById(id);
        return ResponseEntity.ok(student);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudentRequest request) {
        StudentResponse updated = studentService.updateStudent(id, request);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archiveStudent(@PathVariable Long id) {
        studentService.archiveStudent(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/restore")
    public ResponseEntity<StudentResponse> restoreStudent(@PathVariable Long id) {
        StudentResponse restored = studentService.restoreStudent(id);
        return ResponseEntity.ok(restored);
    }
}