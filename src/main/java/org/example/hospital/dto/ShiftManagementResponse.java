package org.example.hospital.dto;

import java.util.List;

public class ShiftManagementResponse {

    private ShiftManagementStats stats;
    private List<ShiftResponse> shifts;
    private ShiftManagementCharts charts;

    public ShiftManagementResponse(ShiftManagementStats stats,
                                   List<ShiftResponse> shifts,
                                   ShiftManagementCharts charts) {
        this.stats = stats;
        this.shifts = shifts;
        this.charts = charts;
    }

    public ShiftManagementStats getStats() {
        return stats;
    }

    public List<ShiftResponse> getShifts() {
        return shifts;
    }

    public ShiftManagementCharts getCharts() {
        return charts;
    }
}
