package org.example.hospital.dto;

import jakarta.validation.constraints.NotNull;
import org.example.hospital.domain.ShiftStatus;

public class UpdateShiftAssignmentRequest {

    private Long assigneeUserId;

    private String notes;

    @NotNull
    private ShiftStatus status;

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

    public ShiftStatus getStatus() {
        return status;
    }

    public void setStatus(ShiftStatus status) {
        this.status = status;
    }
}
