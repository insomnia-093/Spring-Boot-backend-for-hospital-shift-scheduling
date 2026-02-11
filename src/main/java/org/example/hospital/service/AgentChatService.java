package org.example.hospital.service;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.example.hospital.domain.AgentChatMessage;
import org.example.hospital.dto.ChatMessage;
import org.example.hospital.repository.AgentChatMessageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AgentChatService {

    private final AgentChatMessageRepository chatRepository;

    public AgentChatService(AgentChatMessageRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Transactional
    public ChatMessage save(ChatMessage message) {
        String sender = StringUtils.hasText(message.getSender()) ? message.getSender().trim() : "匿名";
        String role = StringUtils.hasText(message.getRole()) ? message.getRole().trim() : "CLIENT";
        String content = StringUtils.hasText(message.getContent()) ? message.getContent().trim() : "";
        OffsetDateTime timestamp = message.getTimestamp() != null ? message.getTimestamp() : OffsetDateTime.now();

        AgentChatMessage saved = chatRepository.save(new AgentChatMessage(sender, role, content, timestamp));
        ChatMessage response = new ChatMessage(saved.getSender(), saved.getRole(), saved.getContent());
        response.setTimestamp(saved.getTimestamp());
        return response;
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> findRecent(int limit) {
        int size = Math.max(1, Math.min(limit, 200));
        List<AgentChatMessage> messages = chatRepository.findAllByOrderByTimestampDesc(
                PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "timestamp"))
        );
        List<ChatMessage> result = messages.stream()
                .map(item -> {
                    ChatMessage dto = new ChatMessage(item.getSender(), item.getRole(), item.getContent());
                    dto.setTimestamp(item.getTimestamp());
                    return dto;
                })
                .collect(Collectors.toList());
        Collections.reverse(result);
        return result;
    }
}
