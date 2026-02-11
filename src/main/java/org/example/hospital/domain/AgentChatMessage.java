package org.example.hospital.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "agent_chat_messages")
public class AgentChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String sender;

    @Column(nullable = false, length = 40)
    private String role;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private OffsetDateTime timestamp;

    protected AgentChatMessage() {
    }

    public AgentChatMessage(String sender, String role, String content, OffsetDateTime timestamp) {
        this.sender = sender;
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }
}
