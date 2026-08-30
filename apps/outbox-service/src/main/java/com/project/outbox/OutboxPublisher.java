package com.project.outbox;

import com.project.outbox.entity.OutboxEvent;
import com.project.outbox.kafka.AuthEventProducer;
import com.project.outbox.repository.OutboxEventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class OutboxPublisher {

    private final OutboxEventRepository repository;
    private final AuthEventProducer producer;

    public OutboxPublisher(
            OutboxEventRepository repository,
            AuthEventProducer producer) {
        this.repository = repository;
        this.producer = producer;
    }

    @Scheduled(fixedDelay = 2000)
    public void publishPendingEvents() {
        for (OutboxEvent event : repository.findByPublishedFalseOrderByIdAsc()) {
            try {
                producer.publish(event.getPayload()).get();

                event.setPublished(true);
                repository.save(event);

                System.out.println(
                        "OUTBOX EVENT PUBLISHED: id=" + event.getId()
                );
            } catch (Exception e) {
                System.err.println(
                        "FAILED TO PUBLISH OUTBOX EVENT: id=" + event.getId()
                );
            }
        }
    }
}
