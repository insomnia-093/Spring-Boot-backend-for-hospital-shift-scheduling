package  org.example.hospital.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.example.hospital.dto.AgentTaskRequest;
import org.example.hospital.dto.AgentTaskResponse;
import org.example.hospital.dto.AgentTaskUpdateRequest;
import org.example.hospital.service.AgentTaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent/tasks")
public class AgentTaskController {

    private final AgentTaskService agentTaskService;

    public AgentTaskController(AgentTaskService agentTaskService) {
        this.agentTaskService = agentTaskService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    // 前端管理员或协调员发起任务，通常用于请求智能体生成排班。
    public ResponseEntity<AgentTaskResponse> createTask(@RequestBody @Valid AgentTaskRequest request) {
        return ResponseEntity.ok(agentTaskService.createTask(request));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    // 智能体轮询未处理的任务列表，按创建时间顺序获取。
    public ResponseEntity<List<AgentTaskResponse>> findPending() {
        return ResponseEntity.ok(agentTaskService.findPendingTasks());
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR','AGENT')")
    // 供前端或智能体查看任务详情与处理状态。
    public ResponseEntity<AgentTaskResponse> findById(@PathVariable Long taskId) {
        return ResponseEntity.ok(agentTaskService.findById(taskId));
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    // 智能体处理完毕后回写结果或更新状态。
    public ResponseEntity<AgentTaskResponse> updateTask(@PathVariable Long taskId,
                                                        @RequestBody @Valid AgentTaskUpdateRequest request) {
        return ResponseEntity.ok(agentTaskService.updateTask(taskId, request));
    }
}
