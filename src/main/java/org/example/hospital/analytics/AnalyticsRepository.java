package org.example.hospital.analytics;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import org.example.hospital.dto.AnalyticsAgentTaskDailyResponse;
import org.example.hospital.dto.AnalyticsAssigneeMonthlyResponse;
import org.example.hospital.dto.AnalyticsDepartmentMonthlyResponse;
import org.example.hospital.dto.AnalyticsShiftDailyResponse;
import org.example.hospital.dto.AnalyticsSummaryResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AnalyticsRepository {

    private final JdbcTemplate jdbcTemplate;

    public AnalyticsRepository(JdbcTemplate analyticsJdbcTemplate) {
        this.jdbcTemplate = analyticsJdbcTemplate;
    }

    public void upsertShiftDaily(LocalDate date,
                                 long departmentId,
                                 String departmentName,
                                 int totalShifts,
                                 int assignedShifts,
                                 int unassignedShifts,
                                 int nightShifts,
                                 int dayShifts) {
        String sql = "INSERT INTO shift_stats_daily " +
            "(stat_date, department_id, department_name, total_shifts, assigned_shifts, unassigned_shifts, night_shifts, day_shifts) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
            "ON CONFLICT (stat_date, department_id) DO UPDATE SET " +
            "department_name = EXCLUDED.department_name, " +
            "total_shifts = EXCLUDED.total_shifts, " +
            "assigned_shifts = EXCLUDED.assigned_shifts, " +
            "unassigned_shifts = EXCLUDED.unassigned_shifts, " +
            "night_shifts = EXCLUDED.night_shifts, " +
            "day_shifts = EXCLUDED.day_shifts";
        jdbcTemplate.update(sql,
            Date.valueOf(date),
            departmentId,
            departmentName,
            totalShifts,
            assignedShifts,
            unassignedShifts,
            nightShifts,
            dayShifts
        );
    }

    public void upsertAssigneeMonthly(LocalDate monthStart,
                                      long assigneeUserId,
                                      String assigneeName,
                                      int shiftCount) {
        String sql = "INSERT INTO assignee_stats_monthly " +
            "(stat_month, assignee_user_id, assignee_name, shift_count) " +
            "VALUES (?, ?, ?, ?) " +
            "ON CONFLICT (stat_month, assignee_user_id) DO UPDATE SET " +
            "assignee_name = EXCLUDED.assignee_name, " +
            "shift_count = EXCLUDED.shift_count";
        jdbcTemplate.update(sql, Date.valueOf(monthStart), assigneeUserId, assigneeName, shiftCount);
    }

    public void upsertDepartmentMonthly(LocalDate monthStart,
                                        long departmentId,
                                        String departmentName,
                                        int shiftCount,
                                        int nightShiftCount,
                                        int dayShiftCount) {
        String sql = "INSERT INTO department_stats_monthly " +
            "(stat_month, department_id, department_name, shift_count, night_shift_count, day_shift_count) " +
            "VALUES (?, ?, ?, ?, ?, ?) " +
            "ON CONFLICT (stat_month, department_id) DO UPDATE SET " +
            "department_name = EXCLUDED.department_name, " +
            "shift_count = EXCLUDED.shift_count, " +
            "night_shift_count = EXCLUDED.night_shift_count, " +
            "day_shift_count = EXCLUDED.day_shift_count";
        jdbcTemplate.update(sql,
            Date.valueOf(monthStart),
            departmentId,
            departmentName,
            shiftCount,
            nightShiftCount,
            dayShiftCount
        );
    }

    public void upsertAgentTaskDaily(LocalDate date,
                                     String taskType,
                                     String status,
                                     int taskCount) {
        String sql = "INSERT INTO agent_task_stats_daily " +
            "(stat_date, task_type, status, task_count) " +
            "VALUES (?, ?, ?, ?) " +
            "ON CONFLICT (stat_date, task_type, status) DO UPDATE SET " +
            "task_count = EXCLUDED.task_count";
        jdbcTemplate.update(sql, Date.valueOf(date), taskType, status, taskCount);
    }

    public AnalyticsSummaryResponse findSummary(LocalDate date) {
        String sql = "SELECT total_shifts, assigned_shifts, unassigned_shifts, night_shifts, day_shifts " +
            "FROM shift_stats_daily WHERE stat_date = ? AND department_id = 0";
        return jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) {
                return new AnalyticsSummaryResponse(date, 0, 0, 0, 0, 0, 0, 0);
            }
            return new AnalyticsSummaryResponse(
                date,
                rs.getInt("total_shifts"),
                rs.getInt("assigned_shifts"),
                rs.getInt("unassigned_shifts"),
                rs.getInt("night_shifts"),
                rs.getInt("day_shifts"),
                countDepartmentsByDate(date),
                countAssigneesByMonth(date.withDayOfMonth(1))
            );
        }, Date.valueOf(date));
    }

    public List<AnalyticsShiftDailyResponse> findShiftDaily(LocalDate date) {
        String sql = "SELECT stat_date, department_id, department_name, total_shifts, assigned_shifts, " +
            "unassigned_shifts, night_shifts, day_shifts " +
            "FROM shift_stats_daily WHERE stat_date = ? AND department_id <> 0 " +
            "ORDER BY total_shifts DESC";
        return jdbcTemplate.query(sql, shiftDailyMapper(), Date.valueOf(date));
    }

    public List<AnalyticsAssigneeMonthlyResponse> findAssigneeMonthly(LocalDate monthStart) {
        String sql = "SELECT stat_month, assignee_user_id, assignee_name, shift_count " +
            "FROM assignee_stats_monthly WHERE stat_month = ? ORDER BY shift_count DESC";
        return jdbcTemplate.query(sql, assigneeMonthlyMapper(), Date.valueOf(monthStart));
    }

    public List<AnalyticsDepartmentMonthlyResponse> findDepartmentMonthly(LocalDate monthStart) {
        String sql = "SELECT stat_month, department_id, department_name, shift_count, night_shift_count, day_shift_count " +
            "FROM department_stats_monthly WHERE stat_month = ? ORDER BY shift_count DESC";
        return jdbcTemplate.query(sql, departmentMonthlyMapper(), Date.valueOf(monthStart));
    }

    public List<AnalyticsAgentTaskDailyResponse> findAgentTaskDaily(LocalDate date) {
        String sql = "SELECT stat_date, task_type, status, task_count " +
            "FROM agent_task_stats_daily WHERE stat_date = ? ORDER BY task_count DESC";
        return jdbcTemplate.query(sql, agentTaskDailyMapper(), Date.valueOf(date));
    }

    public int countDepartmentsByDate(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM shift_stats_daily WHERE stat_date = ? AND department_id <> 0";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, Date.valueOf(date));
        return count == null ? 0 : count;
    }

    public int countAssigneesByMonth(LocalDate monthStart) {
        String sql = "SELECT COUNT(*) FROM assignee_stats_monthly WHERE stat_month = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, Date.valueOf(monthStart));
        return count == null ? 0 : count;
    }

    private RowMapper<AnalyticsShiftDailyResponse> shiftDailyMapper() {
        return (rs, rowNum) -> new AnalyticsShiftDailyResponse(
            rs.getDate("stat_date").toLocalDate(),
            rs.getLong("department_id"),
            rs.getString("department_name"),
            rs.getInt("total_shifts"),
            rs.getInt("assigned_shifts"),
            rs.getInt("unassigned_shifts"),
            rs.getInt("night_shifts"),
            rs.getInt("day_shifts")
        );
    }

    private RowMapper<AnalyticsAssigneeMonthlyResponse> assigneeMonthlyMapper() {
        return (rs, rowNum) -> new AnalyticsAssigneeMonthlyResponse(
            rs.getDate("stat_month").toLocalDate(),
            rs.getLong("assignee_user_id"),
            rs.getString("assignee_name"),
            rs.getInt("shift_count")
        );
    }

    private RowMapper<AnalyticsDepartmentMonthlyResponse> departmentMonthlyMapper() {
        return (rs, rowNum) -> new AnalyticsDepartmentMonthlyResponse(
            rs.getDate("stat_month").toLocalDate(),
            rs.getLong("department_id"),
            rs.getString("department_name"),
            rs.getInt("shift_count"),
            rs.getInt("night_shift_count"),
            rs.getInt("day_shift_count")
        );
    }

    private RowMapper<AnalyticsAgentTaskDailyResponse> agentTaskDailyMapper() {
        return (rs, rowNum) -> new AnalyticsAgentTaskDailyResponse(
            rs.getDate("stat_date").toLocalDate(),
            rs.getString("task_type"),
            rs.getString("status"),
            rs.getInt("task_count")
        );
    }
}
