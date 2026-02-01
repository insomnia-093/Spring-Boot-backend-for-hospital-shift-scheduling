package  org.example.hospital.dto;

import java.time.OffsetDateTime;
import org.example.hospital.domain.AgentTaskStatus;
import org.example.hospital.domain.AgentTaskType;

public class AgentTaskResponse {

    private Long id;
    private AgentTaskType taskType;
    private AgentTaskStatus status;
    private String payload;
    private String result;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public AgentTaskResponse(Long id,
                             AgentTaskType taskType,
                             AgentTaskStatus status,
                             String payload,
                             String result,
                             OffsetDateTime createdAt,
                             OffsetDateTime updatedAt) {
        this.id = id;
        this.taskType = taskType;
        this.status = status;
        this.payload = payload;
        this.result = result;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public AgentTaskType getTaskType() {
        return taskType;
    }

    public AgentTaskStatus getStatus() {
        return status;
    }

    public String getPayload() {
        return payload;
    }

    public String getResult() {
        return result;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
