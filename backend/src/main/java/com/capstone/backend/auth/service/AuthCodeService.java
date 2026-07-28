package com.capstone.backend.auth.service;

import com.capstone.backend.entity.User;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthCodeService {

    private final ConcurrentMap<String, AuthCodeEntry> authCodes = new ConcurrentHashMap<>();
    private final long authCodeExpirationMs;

    public AuthCodeService(@Value("${app.auth.code-expiration-ms:300000}") long authCodeExpirationMs) {
        this.authCodeExpirationMs = authCodeExpirationMs;
    }

    public String issue(User user) {
        cleanupExpiredCodes();

        String code = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusMillis(authCodeExpirationMs);
        authCodes.put(code, new AuthCodeEntry(user, expiresAt));
        return code;
    }

    public User consume(String code) {
        cleanupExpiredCodes();

        if (code == null || code.isBlank()) {
            return null;
        }

        AuthCodeEntry entry = authCodes.remove(code);
        if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
            return null;
        }

        return entry.user();
    }

    private void cleanupExpiredCodes() {
        Instant now = Instant.now();
        authCodes.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private record AuthCodeEntry(
            User user,
            Instant expiresAt
    ) {
    }
}
