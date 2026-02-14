package org.example.hospital.controller;

import org.example.hospital.dto.CozeRequest;
import org.example.hospital.dto.CozeResponse;
import org.example.hospital.service.CozeAgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Coze 智能体 REST API 控制器
 */
@RestController
@RequestMapping("/api/agent")
public class CozeAgentController {

    private final CozeAgentService cozeAgentService;

    public CozeAgentController(CozeAgentService cozeAgentService) {
        this.cozeAgentService = cozeAgentService;
    }

    /**
     * Coze 聊天接口
     * @param request 包含用户输入和用户ID
     * @return Coze 智能体的回复
     */
    @PostMapping("/coze-chat")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR','DOCTOR','NURSE','AGENT')")
    public ResponseEntity<CozeResponse> chat(@RequestBody CozeRequest request) {
        CozeResponse response = cozeAgentService.chat(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 健康检查端点
     */
    @GetMapping("/coze-health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(java.util.Collections.singletonMap("status", "ok"));
    }
}
