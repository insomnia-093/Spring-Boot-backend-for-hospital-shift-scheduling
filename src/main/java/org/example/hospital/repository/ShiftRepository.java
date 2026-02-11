package org.example.hospital.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.example.hospital.domain.Department;
import org.example.hospital.domain.RoleType;
import org.example.hospital.domain.Shift;
import org.example.hospital.domain.ShiftStatus;
import org.example.hospital.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

    List<Shift> findByDepartmentAndStartTimeBetween(Department department, LocalDateTime start, LocalDateTime end);

    List<Shift> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Shift> findByAssignedUser(UserAccount userAccount);

    List<Shift> findByStatus(ShiftStatus status);

    List<Shift> findByRequiredRole(RoleType roleType);
}
