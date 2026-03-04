package org.example.hospital.dto;

public class ShiftManagementStats {

    private long totalShifts;
    private long assignedShifts;
    private long pendingShifts;
    private long nightShifts;

    public ShiftManagementStats(long totalShifts,
                                long assignedShifts,
                                long pendingShifts,
                                long nightShifts) {
        this.totalShifts = totalShifts;
        this.assignedShifts = assignedShifts;
        this.pendingShifts = pendingShifts;
        this.nightShifts = nightShifts;
    }

    public long getTotalShifts() {
        return totalShifts;
    }

    public long getAssignedShifts() {
        return assignedShifts;
    }

    public long getPendingShifts() {
        return pendingShifts;
    }

    public long getNightShifts() {
        return nightShifts;
    }
}
