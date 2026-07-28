package com.capstone.backend.auth.service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SocialSignupSessionService {

    private final ConcurrentMap<String, PendingSocialUser> sessions = new ConcurrentHashMap<>();
    private final long expirationMs;

    public SocialSignupSessionService(@Value("${app.auth.code-expiration-ms:300000}") long expirationMs) {
        this.expirationMs = expirationMs;
    }

    public String issue(PendingSocialUser pendingSocialUser) {
        cleanupExpiredSessions();
        String code = UUID.randomUUID().toString();
        sessions.put(code, pendingSocialUser.withExpiresAt(Instant.now().plusMillis(expirationMs)));
        return code;
    }

    public PendingSocialUser consume(String code) {
        cleanupExpiredSessions();
        if (code == null || code.isBlank()) {
            return null;
        }
        PendingSocialUser session = sessions.remove(code);
        if (session == null || session.expiresAt().isBefore(Instant.now())) {
            return null;
        }
        return session;
    }

    private void cleanupExpiredSessions() {
        Instant now = Instant.now();
        sessions.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    public record PendingSocialUser(
            String provider,
            String providerId,
            String email,
            String name,
            String nickname,
            String profileImageUrl,
            Instant expiresAt
    ) {
        public PendingSocialUser withExpiresAt(Instant newExpiresAt) {
            return new PendingSocialUser(provider, providerId, email, name, nickname, profileImageUrl, newExpiresAt);
        }
    }
}
