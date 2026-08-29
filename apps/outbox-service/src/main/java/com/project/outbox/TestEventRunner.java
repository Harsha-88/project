package com.project.outbox;

import com.project.outbox.event.AuthEvent;
import com.project.outbox.kafka.AuthEventProducer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TestEventRunner implements CommandLineRunner {

    private final AuthEventProducer producer;

    public TestEventRunner(AuthEventProducer producer) {
        this.producer = producer;
    }

    @Override
    public void run(String... args) {
        AuthEvent event = new AuthEvent(
                "USER_LOGIN_SUCCESS",
                101L,
                "audit-check@gmail.com",
                Instant.now(),
                "auth-service"
        );

        producer.publish(event);

        System.out.println("LOGIN EVENT PUBLISHED TO KAFKA");
    }
}
