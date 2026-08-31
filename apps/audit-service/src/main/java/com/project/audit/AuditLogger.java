package com.project.audit;

import com.project.audit.model.AuthEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditLogger {

    private static final Logger logger =
            LoggerFactory.getLogger(AuditLogger.class);

    public void log(AuthEvent event) {
        logger.info(
                "AUDIT EVENT: userId={}, email={}, timestamp={}",
                event.getUserId(),
                event.getEmail(),
                event.getTimestamp()
        );
    }
}
