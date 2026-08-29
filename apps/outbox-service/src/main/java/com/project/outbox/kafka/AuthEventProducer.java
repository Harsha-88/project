package com.project.outbox.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.outbox.event.AuthEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuthEventProducer {

    private static final String TOPIC = "auth-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());

    public AuthEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(AuthEvent event) {
        try {
            String jsonEvent = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, jsonEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize auth event", e);
        }
    }
}
