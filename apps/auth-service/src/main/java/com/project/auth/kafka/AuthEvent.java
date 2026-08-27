package com.project.auth.kafka;

import java.time.Instant;

public class AuthEvent {

    private String eventType;
    private Long userId;
    private String email;
    private Instant timestamp;
    private String source;
    private String version;

    public AuthEvent(
            String eventType,
            Long userId,
            String email,
            Instant timestamp,
            String source,
            String version) {
        this.eventType = eventType;
        this.userId = userId;
        this.email = email;
        this.timestamp = timestamp;
        this.source = source;
        this.version = version;
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

    public String getVersion() {
        return version;
    }
}
