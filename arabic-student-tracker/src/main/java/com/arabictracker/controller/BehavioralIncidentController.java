package com.arabictracker.controller;

import com.arabictracker.service.BehavioralService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/behavioral-incidents")
@RequiredArgsConstructor
public class BehavioralIncidentController {
    
    private final BehavioralService behavioralService;
    
    /**
     * DELETE /api/behavioral-incidents/{incidentId}
     * Delete a specific behavioral incident
     */
    @DeleteMapping("/{incidentId}")
    public ResponseEntity<Void> deleteBehavioralIncident(@PathVariable Long incidentId) {
        behavioralService.deleteBehavioralIncident(incidentId);
        return ResponseEntity.noContent().build();
    }
}