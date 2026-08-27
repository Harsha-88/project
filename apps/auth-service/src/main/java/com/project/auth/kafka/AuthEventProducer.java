package com.project.auth.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuthEventProducer {

    private static final Logger logger =
            LoggerFactory.getLogger(AuthEventProducer.class);

    private static final String TOPIC = "auth-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public AuthEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(AuthEvent event) {
        try {
            String jsonEvent = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(TOPIC, jsonEvent).whenComplete((result, ex) -> {
                if (ex != null) {
                    logger.error("Kafka publish FAILED: {}", jsonEvent, ex);
                } else {
                    logger.info(
                            "Kafka publish SUCCESS: topic={}, partition={}, offset={}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );
                }
            });

            logger.info("Kafka event published: {}", jsonEvent);

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize Kafka event: {}", event, e);
        } catch (Exception e) {
            logger.error("Failed to publish Kafka event", e);
        }
    }
}
