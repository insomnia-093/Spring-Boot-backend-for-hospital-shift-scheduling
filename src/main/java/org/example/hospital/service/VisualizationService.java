package org.example.hospital.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.example.hospital.analytics.AnalyticsService;
import org.example.hospital.domain.DutyCalendarEntry;
import org.example.hospital.domain.UserAccount;
import org.example.hospital.dto.AnalyticsAssigneeMonthlyResponse;
import org.example.hospital.dto.AnalyticsDepartmentMonthlyResponse;
import org.example.hospital.dto.AnalyticsSummaryResponse;
import org.example.hospital.dto.CalendarResponse;
import org.example.hospital.dto.CalendarShiftItem;
import org.example.hospital.dto.OverviewPanelResponse;
import org.example.hospital.dto.ShiftManagementCharts;
import org.example.hospital.dto.ShiftManagementResponse;
import org.example.hospital.dto.ShiftManagementStats;
import org.example.hospital.dto.ShiftResponse;
import org.example.hospital.dto.ShiftTypeRatio;
import org.example.hospital.dto.SummaryItem;
import org.example.hospital.dto.VisualizationResponse;
import org.example.hospital.repository.DutyCalendarEntryRepository;
import org.example.hospital.repository.UserAccountRepository;
import org.example.hospital.security.UserAccountDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VisualizationService {

    private static final LocalTime NIGHT_START = LocalTime.of(18, 0);
    private static final LocalTime NIGHT_END = LocalTime.of(6, 0);

    private final ShiftService shiftService;
    private final DutyCalendarEntryRepository dutyCalendarEntryRepository;
    private final AnalyticsService analyticsService;
    private final UserAccountRepository userAccountRepository;

    public VisualizationService(ShiftService shiftService,
                                DutyCalendarEntryRepository dutyCalendarEntryRepository,
                                AnalyticsService analyticsService,
                                UserAccountRepository userAccountRepository) {
        this.shiftService = shiftService;
        this.dutyCalendarEntryRepository = dutyCalendarEntryRepository;
        this.analyticsService = analyticsService;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional(readOnly = true)
    public VisualizationResponse buildDashboard(LocalDate date, YearMonth month) {
        ShiftManagementResponse shiftManagement = buildShiftManagement(date, month);
        OverviewPanelResponse overviewPanel = buildOverviewPanel(month, shiftManagement.getShifts());
        return new VisualizationResponse(shiftManagement, overviewPanel);
    }

    @Transactional(readOnly = true)
    public ShiftManagementResponse buildShiftManagement(LocalDate date, YearMonth month) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        YearMonth targetMonth = month != null ? month : YearMonth.now();

        List<ShiftResponse> shifts = shiftService.findAll();
        ShiftManagementStats stats = buildStats(targetDate, shifts);
        ShiftManagementCharts charts = buildCharts(targetMonth, shifts, stats);
        return new ShiftManagementResponse(stats, shifts, charts);
    }

    private ShiftManagementStats buildStats(LocalDate date, List<ShiftResponse> shifts) {
        AnalyticsSummaryResponse summary = analyticsService.getSummary(date);
        long totalShifts = summary.getTotalShifts();
        long assignedShifts = summary.getAssignedShifts();
        long pendingShifts = summary.getUnassignedShifts();
        long nightShifts = summary.getNightShifts();

        if (totalShifts == 0 && !shifts.isEmpty()) {
            totalShifts = shifts.size();
            assignedShifts = shifts.stream().filter(s -> s.getAssigneeUserId() != null).count();
            pendingShifts = totalShifts - assignedShifts;
            nightShifts = shifts.stream()
                .filter(s -> isNightShift(s.getStartTime(), s.getEndTime()))
                .count();
        }
        return new ShiftManagementStats(totalShifts, assignedShifts, pendingShifts, nightShifts);
    }

    private ShiftManagementCharts buildCharts(YearMonth month,
                                              List<ShiftResponse> shifts,
                                              ShiftManagementStats stats) {
        long dayShifts = Math.max(0, stats.getTotalShifts() - stats.getNightShifts());
        ShiftTypeRatio ratio = new ShiftTypeRatio(dayShifts, stats.getNightShifts());

        List<SummaryItem> staffDistribution = buildStaffDistribution(month, shifts);
        List<SummaryItem> departmentDistribution = buildDepartmentDistribution(month, shifts);

        return new ShiftManagementCharts(ratio, staffDistribution, departmentDistribution);
    }

    private List<SummaryItem> buildStaffDistribution(YearMonth month, List<ShiftResponse> shifts) {
        List<AnalyticsAssigneeMonthlyResponse> analytics = analyticsService.getAssigneeMonthly(month);
        if (!analytics.isEmpty()) {
            return analytics.stream()
                .map(item -> new SummaryItem(item.getAssigneeName(), item.getShiftCount()))
                .collect(Collectors.toList());
        }

        Map<String, Long> counter = shifts.stream()
            .filter(shift -> shift.getAssigneeUserId() != null)
            .collect(Collectors.groupingBy(
                shift -> shift.getAssigneeName() != null ? shift.getAssigneeName() : "用户" + shift.getAssigneeUserId(),
                Collectors.counting()
            ));

        return counter.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(12)
            .map(entry -> new SummaryItem(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    private List<SummaryItem> buildDepartmentDistribution(YearMonth month, List<ShiftResponse> shifts) {
        List<AnalyticsDepartmentMonthlyResponse> analytics = analyticsService.getDepartmentMonthly(month);
        if (!analytics.isEmpty()) {
            return analytics.stream()
                .map(item -> new SummaryItem(item.getDepartmentName(), item.getShiftCount()))
                .collect(Collectors.toList());
        }

        Map<String, Long> counter = shifts.stream()
            .filter(shift -> shift.getDepartmentName() != null)
            .collect(Collectors.groupingBy(ShiftResponse::getDepartmentName, Collectors.counting()));

        return counter.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .map(entry -> new SummaryItem(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OverviewPanelResponse buildOverviewPanel(YearMonth month) {
        List<ShiftResponse> shifts = shiftService.findAll();
        return buildOverviewPanel(month, shifts);
    }

    private OverviewPanelResponse buildOverviewPanel(YearMonth month, List<ShiftResponse> shifts) {
        List<ShiftResponse> myShifts = resolveMyShifts(shifts);
        CalendarResponse calendar = buildCalendar(month);
        return new OverviewPanelResponse(myShifts, calendar);
    }

    private List<ShiftResponse> resolveMyShifts(List<ShiftResponse> shifts) {
        Long userId = resolveCurrentUserId();
        if (userId == null) {
            return new ArrayList<>();
        }
        return shifts.stream()
            .filter(shift -> Objects.equals(shift.getAssigneeUserId(), userId))
            .sorted(Comparator.comparing(ShiftResponse::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())))
            .collect(Collectors.toList());
    }

    private CalendarResponse buildCalendar(YearMonth month) {
        YearMonth targetMonth = month != null ? month : YearMonth.now();
        LocalDate start = targetMonth.atDay(1);
        LocalDate end = targetMonth.atEndOfMonth();

        List<DutyCalendarEntry> entries = dutyCalendarEntryRepository.findByDateBetween(start, end);
        Map<String, List<CalendarShiftItem>> calendarShifts = new HashMap<>();

        for (DutyCalendarEntry entry : entries) {
            String key = entry.getDate().toString();
            calendarShifts.computeIfAbsent(key, ignored -> new ArrayList<>())
                .add(new CalendarShiftItem(
                    entry.getDepartment() != null ? entry.getDepartment().getId() : null,
                    entry.getDepartment() != null ? entry.getDepartment().getName() : null,
                    entry.getSummary(),
                    entry.getHeadcount()
                ));
        }

        return new CalendarResponse(targetMonth.getYear(), targetMonth.getMonthValue(), calendarShifts);
    }

    private Long resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserAccountDetails details) {
            return details.getId();
        }
        if (principal instanceof UserDetails userDetails) {
            return userAccountRepository.findByEmail(userDetails.getUsername())
                .map(UserAccount::getId)
                .orElse(null);
        }
        if (principal instanceof String username) {
            return userAccountRepository.findByEmail(username)
                .map(UserAccount::getId)
                .orElse(null);
        }
        return null;
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
        return !time.isBefore(NIGHT_START) || time.isBefore(NIGHT_END);
    }
}
