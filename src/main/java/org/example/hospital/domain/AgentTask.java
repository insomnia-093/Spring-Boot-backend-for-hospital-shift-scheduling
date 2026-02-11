package org.example.hospital.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;

@Entity
@Table(name = "agent_tasks")
public class AgentTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private AgentTaskType taskType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AgentTaskStatus status = AgentTaskStatus.PENDING;

    @Lob
    private String payload;

    @Lob
    private String result;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    protected AgentTask() {
    }

    public AgentTask(AgentTaskType taskType, String payload) {
        this.taskType = taskType;
        this.payload = payload;
    }

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public AgentTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(AgentTaskType taskType) {
        this.taskType = taskType;
    }

    public AgentTaskStatus getStatus() {
        return status;
    }

    public void setStatus(AgentTaskStatus status) {
        this.status = status;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void markUpdated() {
        this.updatedAt = OffsetDateTime.now();
    }
}
