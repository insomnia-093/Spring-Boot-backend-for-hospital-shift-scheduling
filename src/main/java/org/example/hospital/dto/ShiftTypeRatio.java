package org.example.hospital.dto;

public class ShiftTypeRatio {

    private long dayShifts;
    private long nightShifts;

    public ShiftTypeRatio(long dayShifts, long nightShifts) {
        this.dayShifts = dayShifts;
        this.nightShifts = nightShifts;
    }

    public long getDayShifts() {
        return dayShifts;
    }

    public long getNightShifts() {
        return nightShifts;
    }
}
