package org.example.hospital.service;

import java.util.Map;
import org.example.hospital.dto.RealtimeEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RealtimePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public RealtimePublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishShiftEvent(String type, Object payload) {
        messagingTemplate.convertAndSend("/topic/shifts", new RealtimeEvent(type, payload));
    }

    public void publishAgentTaskEvent(String type, Object payload) {
        messagingTemplate.convertAndSend("/topic/agent-tasks", new RealtimeEvent(type, payload));
    }

    public void publishNotice(String type, String message) {
        messagingTemplate.convertAndSend("/topic/notifications", new RealtimeEvent(type, Map.of("message", message)));
    }
}
