package  org.example.hospital.service;

import java.util.List;
import java.util.stream.Collectors;
import org.example.hospital.domain.AgentTask;
import org.example.hospital.domain.AgentTaskStatus;
import org.example.hospital.dto.AgentTaskRequest;
import org.example.hospital.dto.AgentTaskResponse;
import org.example.hospital.dto.AgentTaskUpdateRequest;
import org.example.hospital.repository.AgentTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgentTaskService {

    private final AgentTaskRepository agentTaskRepository;
    private final RealtimePublisher realtimePublisher;

    public AgentTaskService(AgentTaskRepository agentTaskRepository, RealtimePublisher realtimePublisher) {
        this.agentTaskRepository = agentTaskRepository;
        this.realtimePublisher = realtimePublisher;
    }

    @Transactional
    public AgentTaskResponse createTask(AgentTaskRequest request) {
        AgentTask task = new AgentTask(request.getTaskType(), request.getPayload());
        AgentTask saved = agentTaskRepository.save(task);
        AgentTaskResponse response = toResponse(saved);
        realtimePublisher.publishAgentTaskEvent("TASK_CREATED", response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<AgentTaskResponse> findPendingTasks() {
        return agentTaskRepository.findByStatusOrderByCreatedAtAsc(AgentTaskStatus.PENDING).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AgentTaskResponse updateTask(Long taskId, AgentTaskUpdateRequest request) {
        AgentTask task = agentTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Agent task not found"));
        task.setStatus(request.getStatus());
        task.setResult(request.getResult());
        task.markUpdated();
        AgentTaskResponse response = toResponse(task);
        realtimePublisher.publishAgentTaskEvent("TASK_UPDATED", response);
        return response;
    }

    @Transactional(readOnly = true)
    public AgentTaskResponse findById(Long taskId) {
        AgentTask task = agentTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Agent task not found"));
        return toResponse(task);
    }

    private AgentTaskResponse toResponse(AgentTask task) {
        return new AgentTaskResponse(
                task.getId(),
                task.getTaskType(),
                task.getStatus(),
                task.getPayload(),
                task.getResult(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
