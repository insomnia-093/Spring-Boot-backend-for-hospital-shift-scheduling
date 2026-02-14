package org.example.hospital.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.example.hospital.domain.Department;
import org.example.hospital.domain.RoleType;
import org.example.hospital.domain.Shift;
import org.example.hospital.domain.ShiftStatus;
import org.example.hospital.domain.UserAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

    @EntityGraph(attributePaths = {"department", "assignedUser"})
    List<Shift> findByDepartmentAndStartTimeBetween(Department department, LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"department", "assignedUser"})
    List<Shift> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"department", "assignedUser"})
    List<Shift> findByAssignedUser(UserAccount userAccount);

    @EntityGraph(attributePaths = {"department", "assignedUser"})
    List<Shift> findByStatus(ShiftStatus status);

    List<Shift> findByRequiredRole(RoleType roleType);

    @EntityGraph(attributePaths = {"department", "assignedUser"})
    @Query("SELECT DISTINCT s FROM Shift s " +
           "LEFT JOIN FETCH s.department " +
           "LEFT JOIN FETCH s.assignedUser " +
           "WHERE s.startTime >= :start AND s.startTime <= :end")
    List<Shift> findByTimeRangeOptimized(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}
