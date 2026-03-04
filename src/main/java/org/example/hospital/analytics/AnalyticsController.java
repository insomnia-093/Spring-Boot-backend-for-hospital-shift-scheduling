package org.example.hospital.analytics;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.example.hospital.dto.AnalyticsAgentTaskDailyResponse;
import org.example.hospital.dto.AnalyticsAssigneeMonthlyResponse;
import org.example.hospital.dto.AnalyticsDepartmentMonthlyResponse;
import org.example.hospital.dto.AnalyticsShiftDailyResponse;
import org.example.hospital.dto.AnalyticsSummaryResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummaryResponse> summary(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(analyticsService.getSummary(date));
    }

    @GetMapping("/shift-daily")
    public ResponseEntity<List<AnalyticsShiftDailyResponse>> shiftDaily(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(analyticsService.getShiftDaily(date));
    }

    @GetMapping("/assignee-monthly")
    public ResponseEntity<List<AnalyticsAssigneeMonthlyResponse>> assigneeMonthly(
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return ResponseEntity.ok(analyticsService.getAssigneeMonthly(month));
    }

    @GetMapping("/department-monthly")
    public ResponseEntity<List<AnalyticsDepartmentMonthlyResponse>> departmentMonthly(
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return ResponseEntity.ok(analyticsService.getDepartmentMonthly(month));
    }

    @GetMapping("/agent-task-daily")
    public ResponseEntity<List<AnalyticsAgentTaskDailyResponse>> agentTaskDaily(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(analyticsService.getAgentTaskDaily(date));
    }

    @PostMapping("/refresh")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> refresh(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        if (date != null) {
            analyticsService.refreshDaily(date);
        }
        if (month != null) {
            analyticsService.refreshMonthly(month);
        }
        return ResponseEntity.accepted().build();
    }
}
