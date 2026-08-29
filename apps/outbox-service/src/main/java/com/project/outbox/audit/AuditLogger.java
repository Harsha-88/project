package com.project.outbox.audit;

import com.project.outbox.event.AuthEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditLogger {

    private static final Logger logger =
            LoggerFactory.getLogger(AuditLogger.class);

    public void log(AuthEvent event) {
        logger.info(
                "AUDIT: eventType={}, userId={}, email={}, timestamp={}, source={}",
                event.getEventType(),
                event.getUserId(),
                event.getEmail(),
                event.getTimestamp(),
                event.getSource()
        );
    }
}
