package org.example.hospital.controller;

import org.example.hospital.dto.ChatMessage;
import org.example.hospital.service.AgentChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class AgentChatController {

    private final AgentChatService agentChatService;

    public AgentChatController(AgentChatService agentChatService) {
        this.agentChatService = agentChatService;
    }

    @MessageMapping("/agent-chat")
    @SendTo("/topic/agent-chat")
    public ChatMessage chat(ChatMessage message) {
        if (message.getRole() == null || message.getRole().isBlank()) {
            message.setRole("CLIENT");
        }
        return agentChatService.save(message);
    }
}
