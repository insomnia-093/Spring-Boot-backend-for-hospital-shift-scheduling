package org.example.hospital.repository;

import java.util.List;
import org.example.hospital.domain.AgentChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentChatMessageRepository extends JpaRepository<AgentChatMessage, Long> {

    List<AgentChatMessage> findAllByOrderByTimestampDesc(Pageable pageable);
}
