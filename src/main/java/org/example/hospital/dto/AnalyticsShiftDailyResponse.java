package org.example.hospital.dto;

import java.time.LocalDate;

public class AnalyticsShiftDailyResponse {

    private final LocalDate date;
    private final Long departmentId;
    private final String departmentName;
    private final int totalShifts;
    private final int assignedShifts;
    private final int unassignedShifts;
    private final int nightShifts;
    private final int dayShifts;

    public AnalyticsShiftDailyResponse(LocalDate date,
                                       Long departmentId,
                                       String departmentName,
                                       int totalShifts,
                                       int assignedShifts,
                                       int unassignedShifts,
                                       int nightShifts,
                                       int dayShifts) {
        this.date = date;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.totalShifts = totalShifts;
        this.assignedShifts = assignedShifts;
        this.unassignedShifts = unassignedShifts;
        this.nightShifts = nightShifts;
        this.dayShifts = dayShifts;
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
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
}
