package com.project.audit.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.audit.AuditLogger;
import com.project.audit.model.AuthEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AuthEventConsumer {

    private static final Logger logger =
            LoggerFactory.getLogger(AuthEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final AuditLogger auditLogger;

    public AuthEventConsumer(
            ObjectMapper objectMapper,
            AuditLogger auditLogger
    ) {
        this.objectMapper = objectMapper;
        this.auditLogger = auditLogger;
    }

    @KafkaListener(topics = "auth-events", groupId = "audit-service")
    public void consume(String event) {
        try {
            AuthEvent authEvent =
                    objectMapper.readValue(event, AuthEvent.class);

            auditLogger.log(authEvent);
        } catch (Exception error) {
            logger.error("Failed to process auth event", error);
        }
    }
}
