package org.example.hospital.dto;

import java.time.LocalDate;

public class AnalyticsAssigneeMonthlyResponse {

    private final LocalDate month;
    private final Long assigneeUserId;
    private final String assigneeName;
    private final int shiftCount;

    public AnalyticsAssigneeMonthlyResponse(LocalDate month,
                                            Long assigneeUserId,
                                            String assigneeName,
                                            int shiftCount) {
        this.month = month;
        this.assigneeUserId = assigneeUserId;
        this.assigneeName = assigneeName;
        this.shiftCount = shiftCount;
    }

    public LocalDate getMonth() {
        return month;
    }

    public Long getAssigneeUserId() {
        return assigneeUserId;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public int getShiftCount() {
        return shiftCount;
    }
}
