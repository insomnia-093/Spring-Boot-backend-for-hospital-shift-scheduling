package org.example.hospital.controller;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.example.hospital.dto.CreateShiftRequest;
import org.example.hospital.dto.ShiftResponse;
import org.example.hospital.dto.ShiftSummaryResponse;
import org.example.hospital.dto.UpdateShiftAssignmentRequest;
import org.example.hospital.service.ShiftService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @GetMapping
    // 前端排班总览页使用，列出全部班次。
    public ResponseEntity<List<ShiftResponse>> listAll() {
        return ResponseEntity.ok(shiftService.findAll());
    }

    @GetMapping("/open")
    // 前端领取班次页面使用，展示仍未指派的班次。
    public ResponseEntity<List<ShiftResponse>> listOpen() {
        return ResponseEntity.ok(shiftService.findOpenShifts());
    }

    @GetMapping("/department/{departmentId}")
    // 按部门和时间范围筛选班次，协调员与智能体都可能调用。
    public ResponseEntity<List<ShiftResponse>> listByDepartment(
            @PathVariable Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(shiftService.findByDepartmentAndRange(departmentId, start, end));
    }

    @GetMapping("/{shiftId}")
    // 详情页调用，展示单个班次的完整信息。
    public ResponseEntity<ShiftResponse> findById(@PathVariable Long shiftId) {
        return ResponseEntity.ok(shiftService.findById(shiftId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    // 协调员在前端创建新的排班需求。
    public ResponseEntity<ShiftResponse> createShift(@RequestBody @Valid CreateShiftRequest request) {
        return ResponseEntity.ok(shiftService.createShift(request));
    }

    @PutMapping("/{shiftId}")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    // 前端指派护士/医生或由智能体建议后修改班次状态。
    public ResponseEntity<ShiftResponse> updateShift(@PathVariable Long shiftId,
                                                     @RequestBody @Valid UpdateShiftAssignmentRequest request) {
        return ResponseEntity.ok(shiftService.updateAssignment(shiftId, request));
    }

    @DeleteMapping("/{shiftId}")
    @PreAuthorize("hasRole('ADMIN')")
    // 管理员在前端删除不需要的班次。
    public ResponseEntity<Void> deleteShift(@PathVariable Long shiftId) {
        shiftService.deleteShift(shiftId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    // 为日历和仪表板分析提供班次摘要信息。
    public ResponseEntity<ShiftSummaryResponse> summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(shiftService.buildSummary(start, end));
    }
}
