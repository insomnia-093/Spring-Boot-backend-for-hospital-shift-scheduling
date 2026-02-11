package org.example.hospital.controller;

import jakarta.validation.Valid;
import org.example.hospital.dto.ShiftResponse;
import org.example.hospital.dto.UpdateShiftRequest;
import org.example.hospital.service.ShiftService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/shifts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminShiftController {

    private final ShiftService shiftService;

    public AdminShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @PutMapping("/{shiftId}")
    // 管理员编辑班次详情（时间、角色、科室、指派、状态、备注）。
    public ResponseEntity<ShiftResponse> updateShift(@PathVariable Long shiftId,
                                                     @RequestBody @Valid UpdateShiftRequest request) {
        return ResponseEntity.ok(shiftService.updateShiftDetails(shiftId, request));
    }
}
