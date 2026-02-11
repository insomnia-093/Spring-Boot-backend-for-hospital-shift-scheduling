package org.example.hospital.dto;

import java.time.OffsetDateTime;

public class ChatMessage {

    private String sender;
    private String role;
    private String content;
    private OffsetDateTime timestamp = OffsetDateTime.now();

    public ChatMessage() {
    }

    public ChatMessage(String sender, String role, String content) {
        this.sender = sender;
        this.role = role;
        this.content = content;
        this.timestamp = OffsetDateTime.now();
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
