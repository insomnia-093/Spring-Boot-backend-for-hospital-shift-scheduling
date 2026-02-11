package org.example.hospital.dto;

import java.time.OffsetDateTime;

public class RealtimeEvent {

    private String type;
    private Object payload;
    private OffsetDateTime timestamp = OffsetDateTime.now();

    public RealtimeEvent() {
    }

    public RealtimeEvent(String type, Object payload) {
        this.type = type;
        this.payload = payload;
        this.timestamp = OffsetDateTime.now();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
