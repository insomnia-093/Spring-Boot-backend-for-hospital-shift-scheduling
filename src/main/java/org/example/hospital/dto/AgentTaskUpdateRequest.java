package  org.example.hospital.dto;

import jakarta.validation.constraints.NotNull;
import org.example.hospital.domain.AgentTaskStatus;

public class AgentTaskUpdateRequest {

    @NotNull
    private AgentTaskStatus status;

    private String result;

    public AgentTaskStatus getStatus() {
        return status;
    }

    public void setStatus(AgentTaskStatus status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
