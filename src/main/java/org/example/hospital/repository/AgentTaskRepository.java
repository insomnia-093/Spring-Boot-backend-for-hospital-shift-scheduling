package org.example.hospital.repository;

import java.util.List;
import org.example.hospital.domain.AgentTask;
import org.example.hospital.domain.AgentTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentTaskRepository extends JpaRepository<AgentTask, Long> {

    List<AgentTask> findByStatusOrderByCreatedAtAsc(AgentTaskStatus status);

    List<AgentTask> findByCreatedAtBetween(java.time.OffsetDateTime start,
                                           java.time.OffsetDateTime end);
}
