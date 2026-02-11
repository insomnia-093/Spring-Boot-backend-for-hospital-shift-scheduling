package org.example.hospital.repository;

import java.time.LocalDate;
import java.util.List;
import org.example.hospital.domain.DutyCalendarEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DutyCalendarEntryRepository extends JpaRepository<DutyCalendarEntry, Long> {

    List<DutyCalendarEntry> findByDateBetween(LocalDate start, LocalDate end);
}
