package  org.example.hospital.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.example.hospital.domain.Department;
import org.example.hospital.domain.RoleType;
import org.example.hospital.domain.Shift;
import org.example.hospital.domain.ShiftStatus;
import org.example.hospital.domain.UserAccount;
import org.example.hospital.dto.CreateShiftRequest;
import org.example.hospital.dto.ShiftResponse;
import org.example.hospital.dto.UpdateShiftAssignmentRequest;
import org.example.hospital.repository.ShiftRepository;
import org.example.hospital.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final DepartmentService departmentService;
    private final UserAccountRepository userAccountRepository;

    public ShiftService(ShiftRepository shiftRepository,
                        DepartmentService departmentService,
                        UserAccountRepository userAccountRepository) {
        this.shiftRepository = shiftRepository;
        this.departmentService = departmentService;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    public ShiftResponse createShift(CreateShiftRequest request) {
        validateShiftTimes(request.getStartTime(), request.getEndTime());
        Department department = departmentService.requireById(request.getDepartmentId());
        Shift shift = new Shift(request.getStartTime(), request.getEndTime(), request.getRequiredRole(), department);
        shift.setNotes(request.getNotes());
        Shift saved = shiftRepository.save(shift);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ShiftResponse> findAll() {
        return shiftRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ShiftResponse findById(Long id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found"));
        return toResponse(shift);
    }

    @Transactional(readOnly = true)
    public List<ShiftResponse> findOpenShifts() {
        return shiftRepository.findByStatus(ShiftStatus.OPEN).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShiftResponse> findByDepartmentAndRange(Long departmentId, LocalDateTime start, LocalDateTime end) {
        Department department = departmentService.requireById(departmentId);
        LocalDateTime effectiveStart = start != null ? start : LocalDateTime.now().minusDays(7);
        LocalDateTime effectiveEnd = end != null ? end : LocalDateTime.now().plusDays(30);
        return shiftRepository.findByDepartmentAndStartTimeBetween(department, effectiveStart, effectiveEnd)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ShiftResponse updateAssignment(Long shiftId, UpdateShiftAssignmentRequest request) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found"));
        if (request.getAssigneeUserId() != null) {
            UserAccount assignee = userAccountRepository.findById(request.getAssigneeUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            ensureRoleMatches(assignee, shift.getRequiredRole());
            shift.setAssignedUser(assignee);
        } else {
            shift.setAssignedUser(null);
        }
        shift.setNotes(request.getNotes());
        shift.setStatus(request.getStatus());
        return toResponse(shift);
    }

    @Transactional
    public void deleteShift(Long shiftId) {
        if (!shiftRepository.existsById(shiftId)) {
            throw new IllegalArgumentException("Shift not found");
        }
        shiftRepository.deleteById(shiftId);
    }

    private void validateShiftTimes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Shift time cannot be null");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Shift end must be after start");
        }
    }

    private void ensureRoleMatches(UserAccount userAccount, RoleType requiredRole) {
        boolean hasRole = userAccount.getRoles().stream()
                .map(role -> role.getName())
                .filter(Objects::nonNull)
                .anyMatch(roleType -> roleType == requiredRole);
        if (!hasRole) {
            throw new IllegalArgumentException("User does not have the required role");
        }
    }

    private ShiftResponse toResponse(Shift shift) {
        Long assigneeId = shift.getAssignedUser() != null ? shift.getAssignedUser().getId() : null;
        String assigneeName = shift.getAssignedUser() != null ? shift.getAssignedUser().getFullName() : null;
        return new ShiftResponse(
                shift.getId(),
                shift.getStartTime(),
                shift.getEndTime(),
                shift.getRequiredRole(),
                shift.getStatus(),
                shift.getDepartment().getId(),
                shift.getDepartment().getName(),
                assigneeId,
                assigneeName,
                shift.getNotes()
        );
    }
}
