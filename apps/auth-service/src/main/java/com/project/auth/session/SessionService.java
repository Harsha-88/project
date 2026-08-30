package com.project.auth.session;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class SessionService {

    private final StringRedisTemplate redisTemplate;

    public SessionService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveSession(String sessionToken, String email) {
        redisTemplate.opsForValue().set(
                "session:" + sessionToken,
                email,
                Duration.ofHours(24)
        );
    }

    public String getSessionEmail(String sessionToken) {
        return redisTemplate.opsForValue().get("session:" + sessionToken);
    }
}
