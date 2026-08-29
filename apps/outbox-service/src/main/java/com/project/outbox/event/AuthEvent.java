package com.project.outbox.event;

import java.time.Instant;

public class AuthEvent {

    private String eventType;
    private Long userId;
    private String email;
    private Instant timestamp;
    private String source;

    public AuthEvent(
            String eventType,
            Long userId,
            String email,
            Instant timestamp,
            String source) {
        this.eventType = eventType;
        this.userId = userId;
        this.email = email;
        this.timestamp = timestamp;
        this.source = source;
    }

    public String getEventType() {
        return eventType;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getSource() {
        return source;
    }
}
