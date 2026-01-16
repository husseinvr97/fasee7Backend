package com.arabictracker.controller;

import com.arabictracker.dto.requestDto.UpdateParentRequest;
import com.arabictracker.dto.responseDto.ParentSummary;
import com.arabictracker.dto.responseDto.ParentWithStudentsResponse;
import com.arabictracker.service.ParentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
public class ParentController {
    
    private final ParentService parentService;
    
    @GetMapping("/{id}/students")
    public ResponseEntity<ParentWithStudentsResponse> getParentWithStudents(
            @PathVariable Long id) {
        ParentWithStudentsResponse response = parentService.getParentWithStudents(id);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ParentSummary> updateParent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateParentRequest request) {
        ParentSummary updated = parentService.updateParent(id, request);
        return ResponseEntity.ok(updated);
    }
}