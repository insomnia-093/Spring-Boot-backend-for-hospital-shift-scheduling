package org.example.hospital.analytics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.example.hospital.domain.AgentTask;
import org.example.hospital.domain.AgentTaskStatus;
import org.example.hospital.domain.AgentTaskType;
import org.example.hospital.domain.Department;
import org.example.hospital.domain.Shift;
import org.example.hospital.domain.UserAccount;
import org.example.hospital.dto.AnalyticsAgentTaskDailyResponse;
import org.example.hospital.dto.AnalyticsAssigneeMonthlyResponse;
import org.example.hospital.dto.AnalyticsDepartmentMonthlyResponse;
import org.example.hospital.dto.AnalyticsShiftDailyResponse;
import org.example.hospital.dto.AnalyticsSummaryResponse;
import org.example.hospital.repository.AgentTaskRepository;
import org.example.hospital.repository.ShiftRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {

    private static final long ALL_DEPARTMENT_ID = 0L;
    private static final String ALL_DEPARTMENT_NAME = "全部科室";

    private final ShiftRepository shiftRepository;
    private final AgentTaskRepository agentTaskRepository;
    private final AnalyticsRepository analyticsRepository;

    @Value("${app.analytics.enabled:true}")
    private boolean analyticsEnabled;

    public AnalyticsService(ShiftRepository shiftRepository,
                            AgentTaskRepository agentTaskRepository,
                            AnalyticsRepository analyticsRepository) {
        this.shiftRepository = shiftRepository;
        this.agentTaskRepository = agentTaskRepository;
        this.analyticsRepository = analyticsRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse getSummary(LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return analyticsRepository.findSummary(targetDate);
    }

    @Transactional(readOnly = true)
    public List<AnalyticsShiftDailyResponse> getShiftDaily(LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return analyticsRepository.findShiftDaily(targetDate);
    }

    @Transactional(readOnly = true)
    public List<AnalyticsAssigneeMonthlyResponse> getAssigneeMonthly(YearMonth month) {
        YearMonth targetMonth = month != null ? month : YearMonth.now();
        return analyticsRepository.findAssigneeMonthly(targetMonth.atDay(1));
    }

    @Transactional(readOnly = true)
    public List<AnalyticsDepartmentMonthlyResponse> getDepartmentMonthly(YearMonth month) {
        YearMonth targetMonth = month != null ? month : YearMonth.now();
        return analyticsRepository.findDepartmentMonthly(targetMonth.atDay(1));
    }

    @Transactional(readOnly = true)
    public List<AnalyticsAgentTaskDailyResponse> getAgentTaskDaily(LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return analyticsRepository.findAgentTaskDaily(targetDate);
    }

    @Transactional
    public void refreshDaily(LocalDate date) {
        if (!analyticsEnabled) {
            return;
        }
        LocalDate targetDate = date != null ? date : LocalDate.now();
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.plusDays(1).atStartOfDay().minusNanos(1);
        List<Shift> shifts = shiftRepository.findByTimeRangeOptimized(start, end);

        Map<Department, List<Shift>> byDept = shifts.stream()
            .filter(shift -> shift.getDepartment() != null)
            .collect(Collectors.groupingBy(Shift::getDepartment));

        int totalShifts = shifts.size();
        int assignedShifts = (int) shifts.stream().filter(shift -> shift.getAssignedUser() != null).count();
        int unassignedShifts = totalShifts - assignedShifts;
        int nightShifts = (int) shifts.stream().filter(shift -> isNightShift(shift.getStartTime(), shift.getEndTime())).count();
        int dayShifts = totalShifts - nightShifts;

        analyticsRepository.upsertShiftDaily(
            targetDate,
            ALL_DEPARTMENT_ID,
            ALL_DEPARTMENT_NAME,
            totalShifts,
            assignedShifts,
            unassignedShifts,
            nightShifts,
            dayShifts
        );

        for (Map.Entry<Department, List<Shift>> entry : byDept.entrySet()) {
            Department department = entry.getKey();
            List<Shift> deptShifts = entry.getValue();
            int deptTotal = deptShifts.size();
            int deptAssigned = (int) deptShifts.stream().filter(shift -> shift.getAssignedUser() != null).count();
            int deptUnassigned = deptTotal - deptAssigned;
            int deptNight = (int) deptShifts.stream().filter(shift -> isNightShift(shift.getStartTime(), shift.getEndTime())).count();
            int deptDay = deptTotal - deptNight;

            analyticsRepository.upsertShiftDaily(
                targetDate,
                department.getId(),
                department.getName(),
                deptTotal,
                deptAssigned,
                deptUnassigned,
                deptNight,
                deptDay
            );
        }

        refreshAgentTaskDaily(targetDate);
    }

    @Transactional
    public void refreshMonthly(YearMonth month) {
        if (!analyticsEnabled) {
            return;
        }
        YearMonth targetMonth = month != null ? month : YearMonth.now();
        LocalDateTime start = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime end = targetMonth.plusMonths(1).atDay(1).atStartOfDay().minusNanos(1);
        LocalDate monthStart = targetMonth.atDay(1);

        List<Shift> shifts = shiftRepository.findByTimeRangeOptimized(start, end);

        Map<UserAccount, Long> assigneeCounts = shifts.stream()
            .map(Shift::getAssignedUser)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(user -> user, Collectors.counting()));

        for (Map.Entry<UserAccount, Long> entry : assigneeCounts.entrySet()) {
            UserAccount user = entry.getKey();
            int count = entry.getValue().intValue();
            analyticsRepository.upsertAssigneeMonthly(
                monthStart,
                user.getId(),
                user.getFullName(),
                count
            );
        }

        Map<Department, List<Shift>> byDept = shifts.stream()
            .filter(shift -> shift.getDepartment() != null)
            .collect(Collectors.groupingBy(Shift::getDepartment));

        for (Map.Entry<Department, List<Shift>> entry : byDept.entrySet()) {
            Department department = entry.getKey();
            List<Shift> deptShifts = entry.getValue();
            int total = deptShifts.size();
            int night = (int) deptShifts.stream().filter(shift -> isNightShift(shift.getStartTime(), shift.getEndTime())).count();
            int day = total - night;

            analyticsRepository.upsertDepartmentMonthly(
                monthStart,
                department.getId(),
                department.getName(),
                total,
                night,
                day
            );
        }
    }

    @Transactional
    public void refreshAgentTaskDaily(LocalDate date) {
        if (!analyticsEnabled) {
            return;
        }
        LocalDate targetDate = date != null ? date : LocalDate.now();
        OffsetDateTime start = targetDate.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime end = targetDate.plusDays(1).atStartOfDay().minusNanos(1).atOffset(OffsetDateTime.now().getOffset());
        List<AgentTask> tasks = agentTaskRepository.findByCreatedAtBetween(start, end);

        Map<String, Integer> counter = new HashMap<>();
        for (AgentTask task : tasks) {
            AgentTaskType type = task.getTaskType();
            AgentTaskStatus status = task.getStatus();
            String key = type.name() + ":" + status.name();
            counter.put(key, counter.getOrDefault(key, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : counter.entrySet()) {
            String[] parts = entry.getKey().split(":", 2);
            analyticsRepository.upsertAgentTaskDaily(
                targetDate,
                parts[0],
                parts[1],
                entry.getValue()
            );
        }
    }

    private boolean isNightShift(LocalDateTime start, LocalDateTime end) {
        if (start == null) {
            return false;
        }
        if (isNightTime(start.toLocalTime())) {
            return true;
        }
        return end != null && isNightTime(end.toLocalTime());
    }

    private boolean isNightTime(LocalTime time) {
        return time.isAfter(LocalTime.of(17, 59)) || time.isBefore(LocalTime.of(6, 0));
    }
}
