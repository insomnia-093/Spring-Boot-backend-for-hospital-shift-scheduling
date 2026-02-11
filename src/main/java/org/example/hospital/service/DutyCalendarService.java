package org.example.hospital.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.example.hospital.domain.Department;
import org.example.hospital.domain.DutyCalendarEntry;
import org.example.hospital.dto.CreateDutyCalendarEntryRequest;
import org.example.hospital.dto.DutyCalendarEntryResponse;
import org.example.hospital.repository.DutyCalendarEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DutyCalendarService {

    private final DutyCalendarEntryRepository dutyCalendarEntryRepository;
    private final DepartmentService departmentService;

    public DutyCalendarService(DutyCalendarEntryRepository dutyCalendarEntryRepository,
                               DepartmentService departmentService) {
        this.dutyCalendarEntryRepository = dutyCalendarEntryRepository;
        this.departmentService = departmentService;
    }

    @Transactional
    public DutyCalendarEntryResponse createEntry(CreateDutyCalendarEntryRequest request) {
        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentService.requireById(request.getDepartmentId());
        }
        DutyCalendarEntry entry = new DutyCalendarEntry(request.getDate(), department, request.getSummary(), request.getHeadcount());
        DutyCalendarEntry saved = dutyCalendarEntryRepository.save(entry);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DutyCalendarEntryResponse> listByRange(LocalDate start, LocalDate end) {
        LocalDate effectiveStart = start != null ? start : LocalDate.now().withDayOfMonth(1);
        LocalDate effectiveEnd = end != null ? end : effectiveStart.plusMonths(1).minusDays(1);
        return dutyCalendarEntryRepository.findByDateBetween(effectiveStart, effectiveEnd)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private DutyCalendarEntryResponse toResponse(DutyCalendarEntry entry) {
        Department department = entry.getDepartment();
        return new DutyCalendarEntryResponse(
                entry.getId(),
                entry.getDate(),
                department != null ? department.getId() : null,
                department != null ? department.getName() : null,
                entry.getSummary(),
                entry.getHeadcount()
        );
    }
}
