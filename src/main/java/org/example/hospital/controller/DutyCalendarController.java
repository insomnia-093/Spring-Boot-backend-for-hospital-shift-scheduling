package org.example.hospital.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.example.hospital.dto.CreateDutyCalendarEntryRequest;
import org.example.hospital.dto.DutyCalendarEntryResponse;
import org.example.hospital.service.DutyCalendarService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calendar")
public class DutyCalendarController {

    private final DutyCalendarService dutyCalendarService;

    public DutyCalendarController(DutyCalendarService dutyCalendarService) {
        this.dutyCalendarService = dutyCalendarService;
    }

    @GetMapping
    public ResponseEntity<List<DutyCalendarEntryResponse>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(dutyCalendarService.listByRange(start, end));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    public ResponseEntity<DutyCalendarEntryResponse> create(@RequestBody @Valid CreateDutyCalendarEntryRequest request) {
        return ResponseEntity.ok(dutyCalendarService.createEntry(request));
    }
}
