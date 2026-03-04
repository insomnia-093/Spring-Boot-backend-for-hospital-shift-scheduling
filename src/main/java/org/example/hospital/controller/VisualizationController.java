package org.example.hospital.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import org.example.hospital.dto.OverviewPanelResponse;
import org.example.hospital.dto.ShiftManagementResponse;
import org.example.hospital.dto.VisualizationResponse;
import org.example.hospital.service.VisualizationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/visualization")
public class VisualizationController {

    private final VisualizationService visualizationService;

    public VisualizationController(VisualizationService visualizationService) {
        this.visualizationService = visualizationService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR','DOCTOR','NURSE','AGENT')")
    public ResponseEntity<VisualizationResponse> dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return ResponseEntity.ok(visualizationService.buildDashboard(date, month));
    }

    @GetMapping("/shift-management")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR','DOCTOR','NURSE','AGENT')")
    public ResponseEntity<ShiftManagementResponse> shiftManagement(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return ResponseEntity.ok(visualizationService.buildShiftManagement(date, month));
    }

    @GetMapping("/overview-panel")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR','DOCTOR','NURSE','AGENT')")
    public ResponseEntity<OverviewPanelResponse> overviewPanel(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return ResponseEntity.ok(visualizationService.buildOverviewPanel(month));
    }
}
