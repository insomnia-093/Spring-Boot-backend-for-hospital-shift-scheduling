package org.example.hospital.dto;

import java.time.LocalDate;

public class AnalyticsAgentTaskDailyResponse {

    private final LocalDate date;
    private final String taskType;
    private final String status;
    private final int taskCount;

    public AnalyticsAgentTaskDailyResponse(LocalDate date, String taskType, String status, int taskCount) {
        this.date = date;
        this.taskType = taskType;
        this.status = status;
        this.taskCount = taskCount;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getTaskType() {
        return taskType;
    }

    public String getStatus() {
        return status;
    }

    public int getTaskCount() {
        return taskCount;
    }
}
