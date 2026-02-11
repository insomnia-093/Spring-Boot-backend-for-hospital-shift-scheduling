package  org.example.hospital.dto;

import java.util.List;

public class ShiftSummaryResponse {

    private long totalShifts;
    private long nightShifts;
    private long assignedShifts;
    private long unassignedShifts;
    private long totalAssignees;
    private List<SummaryItem> roleDistribution;
    private List<SummaryItem> departmentDistribution;
    private List<SummaryItem> assigneeDistribution;

    public ShiftSummaryResponse(long totalShifts,
                                long nightShifts,
                                long assignedShifts,
                                long unassignedShifts,
                                long totalAssignees,
                                List<SummaryItem> roleDistribution,
                                List<SummaryItem> departmentDistribution,
                                List<SummaryItem> assigneeDistribution) {
        this.totalShifts = totalShifts;
        this.nightShifts = nightShifts;
        this.assignedShifts = assignedShifts;
        this.unassignedShifts = unassignedShifts;
        this.totalAssignees = totalAssignees;
        this.roleDistribution = roleDistribution;
        this.departmentDistribution = departmentDistribution;
        this.assigneeDistribution = assigneeDistribution;
    }

    public long getTotalShifts() {
        return totalShifts;
    }

    public long getNightShifts() {
        return nightShifts;
    }

    public long getAssignedShifts() {
        return assignedShifts;
    }

    public long getUnassignedShifts() {
        return unassignedShifts;
    }

    public long getTotalAssignees() {
        return totalAssignees;
    }

    public List<SummaryItem> getRoleDistribution() {
        return roleDistribution;
    }

    public List<SummaryItem> getDepartmentDistribution() {
        return departmentDistribution;
    }

    public List<SummaryItem> getAssigneeDistribution() {
        return assigneeDistribution;
    }
}
