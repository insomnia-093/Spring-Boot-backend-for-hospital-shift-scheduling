package org.example.hospital.dto;

public class VisualizationResponse {

    private ShiftManagementResponse shiftManagement;
    private OverviewPanelResponse overviewPanel;

    public VisualizationResponse(ShiftManagementResponse shiftManagement,
                                 OverviewPanelResponse overviewPanel) {
        this.shiftManagement = shiftManagement;
        this.overviewPanel = overviewPanel;
    }

    public ShiftManagementResponse getShiftManagement() {
        return shiftManagement;
    }

    public OverviewPanelResponse getOverviewPanel() {
        return overviewPanel;
    }
}
