package com.project.audit.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AuthEventConsumer {

    private static final Logger logger =
            LoggerFactory.getLogger(AuthEventConsumer.class);

    @KafkaListener(topics = "auth-events", groupId = "audit-service")
    public void consume(String event) {
        logger.info("AUDIT EVENT received");
    }
}
