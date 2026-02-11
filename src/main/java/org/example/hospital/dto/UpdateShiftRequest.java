package org.example.hospital.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import org.example.hospital.domain.RoleType;
import org.example.hospital.domain.ShiftStatus;

public class UpdateShiftRequest {

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    @NotNull
    private RoleType requiredRole;

    @NotNull
    private ShiftStatus status;

    @NotNull
    private Long departmentId;

    private Long assigneeUserId;

    private String notes;

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public RoleType getRequiredRole() {
        return requiredRole;
    }

    public void setRequiredRole(RoleType requiredRole) {
        this.requiredRole = requiredRole;
    }

    public ShiftStatus getStatus() {
        return status;
    }

    public void setStatus(ShiftStatus status) {
        this.status = status;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getAssigneeUserId() {
        return assigneeUserId;
    }

    public void setAssigneeUserId(Long assigneeUserId) {
        this.assigneeUserId = assigneeUserId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
