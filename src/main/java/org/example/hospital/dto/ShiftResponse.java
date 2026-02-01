package org.example.hospital.dto;

import java.time.LocalDateTime;
import org.example.hospital.domain.RoleType;
import org.example.hospital.domain.ShiftStatus;

public class ShiftResponse {

    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private RoleType requiredRole;
    private ShiftStatus status;
    private Long departmentId;
    private String departmentName;
    private Long assigneeUserId;
    private String assigneeName;
    private String notes;

    public ShiftResponse(Long id,
                         LocalDateTime startTime,
                         LocalDateTime endTime,
                         RoleType requiredRole,
                         ShiftStatus status,
                         Long departmentId,
                         String departmentName,
                         Long assigneeUserId,
                         String assigneeName,
                         String notes) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.requiredRole = requiredRole;
        this.status = status;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.assigneeUserId = assigneeUserId;
        this.assigneeName = assigneeName;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public RoleType getRequiredRole() {
        return requiredRole;
    }

    public ShiftStatus getStatus() {
        return status;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public Long getAssigneeUserId() {
        return assigneeUserId;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public String getNotes() {
        return notes;
    }
}
