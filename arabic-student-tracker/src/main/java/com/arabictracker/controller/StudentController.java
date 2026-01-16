package com.arabictracker.controller;

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
public ResponseEntity<List<StudentResponse>> getAllStudents() {  // Change method name
    List<StudentResponse> students = studentService.getAllStudents();  // Change service call
    return ResponseEntity.ok(students);
}
    
    @GetMapping("/archived")
    public ResponseEntity<List<StudentResponse>> getArchivedStudents() {
        List<StudentResponse> students = studentService.getArchivedStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/search")
    public ResponseEntity<List<StudentResponse>> searchStudents(
        @RequestParam String query) {
    List<StudentResponse> students = studentService.searchStudents(query);
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