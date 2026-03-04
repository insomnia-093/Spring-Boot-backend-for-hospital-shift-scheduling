package org.example.hospital.dto;

import java.time.LocalDate;

public class AnalyticsSummaryResponse {

    private final LocalDate date;
    private final int totalShifts;
    private final int assignedShifts;
    private final int unassignedShifts;
    private final int nightShifts;
    private final int dayShifts;
    private final int departmentCount;
    private final int assigneeCount;

    public AnalyticsSummaryResponse(LocalDate date,
                                    int totalShifts,
                                    int assignedShifts,
                                    int unassignedShifts,
                                    int nightShifts,
                                    int dayShifts,
                                    int departmentCount,
                                    int assigneeCount) {
        this.date = date;
        this.totalShifts = totalShifts;
        this.assignedShifts = assignedShifts;
        this.unassignedShifts = unassignedShifts;
        this.nightShifts = nightShifts;
        this.dayShifts = dayShifts;
        this.departmentCount = departmentCount;
        this.assigneeCount = assigneeCount;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getTotalShifts() {
        return totalShifts;
    }

    public int getAssignedShifts() {
        return assignedShifts;
    }

    public int getUnassignedShifts() {
        return unassignedShifts;
    }

    public int getNightShifts() {
        return nightShifts;
    }

    public int getDayShifts() {
        return dayShifts;
    }

    public int getDepartmentCount() {
        return departmentCount;
    }

    public int getAssigneeCount() {
        return assigneeCount;
    }
}
