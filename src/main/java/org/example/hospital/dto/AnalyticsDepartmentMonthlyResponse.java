package org.example.hospital.dto;

import java.time.LocalDate;

public class AnalyticsDepartmentMonthlyResponse {

    private final LocalDate month;
    private final Long departmentId;
    private final String departmentName;
    private final int shiftCount;
    private final int nightShiftCount;
    private final int dayShiftCount;

    public AnalyticsDepartmentMonthlyResponse(LocalDate month,
                                              Long departmentId,
                                              String departmentName,
                                              int shiftCount,
                                              int nightShiftCount,
                                              int dayShiftCount) {
        this.month = month;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.shiftCount = shiftCount;
        this.nightShiftCount = nightShiftCount;
        this.dayShiftCount = dayShiftCount;
    }

    public LocalDate getMonth() {
        return month;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public int getShiftCount() {
        return shiftCount;
    }

    public int getNightShiftCount() {
        return nightShiftCount;
    }

    public int getDayShiftCount() {
        return dayShiftCount;
    }
}
