package org.example.hospital.dto;

import java.util.List;
import java.util.Map;

public class CalendarResponse {

    private int year;
    private int month;
    private Map<String, List<CalendarShiftItem>> calendarShifts;

    public CalendarResponse(int year,
                            int month,
                            Map<String, List<CalendarShiftItem>> calendarShifts) {
        this.year = year;
        this.month = month;
        this.calendarShifts = calendarShifts;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public Map<String, List<CalendarShiftItem>> getCalendarShifts() {
        return calendarShifts;
    }
}
