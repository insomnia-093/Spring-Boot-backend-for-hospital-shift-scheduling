package org.example.hospital.dto;

import java.util.List;

public class OverviewPanelResponse {

    private List<ShiftResponse> myShifts;
    private CalendarResponse calendar;

    public OverviewPanelResponse(List<ShiftResponse> myShifts,
                                 CalendarResponse calendar) {
        this.myShifts = myShifts;
        this.calendar = calendar;
    }

    public List<ShiftResponse> getMyShifts() {
        return myShifts;
    }

    public CalendarResponse getCalendar() {
        return calendar;
    }
}
