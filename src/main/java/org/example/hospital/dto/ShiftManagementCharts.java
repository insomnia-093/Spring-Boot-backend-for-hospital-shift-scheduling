package org.example.hospital.dto;

import java.util.List;

public class ShiftManagementCharts {

    private ShiftTypeRatio shiftTypeRatio;
    private List<SummaryItem> staffDistribution;
    private List<SummaryItem> departmentDistribution;

    public ShiftManagementCharts(ShiftTypeRatio shiftTypeRatio,
                                 List<SummaryItem> staffDistribution,
                                 List<SummaryItem> departmentDistribution) {
        this.shiftTypeRatio = shiftTypeRatio;
        this.staffDistribution = staffDistribution;
        this.departmentDistribution = departmentDistribution;
    }

    public ShiftTypeRatio getShiftTypeRatio() {
        return shiftTypeRatio;
    }

    public List<SummaryItem> getStaffDistribution() {
        return staffDistribution;
    }

    public List<SummaryItem> getDepartmentDistribution() {
        return departmentDistribution;
    }
}
