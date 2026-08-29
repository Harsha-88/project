package com.project.audit.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AuthEventConsumer {

    @KafkaListener(topics = "auth-events", groupId = "audit-service")
    public void consume(String event) {
        System.out.println("AUDIT EVENT: " + event);
    }
}
