package org.example.hospital.controller;

import java.util.List;
import org.example.hospital.dto.ChatMessage;
import org.example.hospital.service.AgentChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent/chat")
public class AgentChatHistoryController {

    private final AgentChatService agentChatService;

    public AgentChatHistoryController(AgentChatService agentChatService) {
        this.agentChatService = agentChatService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR','DOCTOR','NURSE','AGENT')")
    // 读取最近的智能体沟通记录。
    public ResponseEntity<List<ChatMessage>> findRecent(@RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(agentChatService.findRecent(limit));
    }
}
