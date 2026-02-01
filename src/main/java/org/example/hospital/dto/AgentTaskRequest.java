package  org.example.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.hospital.domain.AgentTaskType;

public class AgentTaskRequest {

    @NotNull
    private AgentTaskType taskType;

    @NotBlank
    private String payload;

    public AgentTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(AgentTaskType taskType) {
        this.taskType = taskType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
