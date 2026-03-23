package com.rentify.core.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TokenRevocationService {

    private static final Duration FALLBACK_TTL = Duration.ofHours(24);

    private final JwtService jwtService;
    private final Map<String, Instant> revokedTokensByValue = new ConcurrentHashMap<>();

    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        Instant expiresAt = resolveExpiration(token);
        revokedTokensByValue.put(token, expiresAt);
    }

    public boolean isRevoked(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        Instant expiresAt = revokedTokensByValue.get(token);
        if (expiresAt == null) {
            return false;
        }

        if (expiresAt.isBefore(Instant.now())) {
            revokedTokensByValue.remove(token, expiresAt);
            return false;
        }

        return true;
    }

    private Instant resolveExpiration(String token) {
        try {
            Date expiration = jwtService.extractExpiration(token);
            if (expiration != null) {
                return expiration.toInstant();
            }
        } catch (RuntimeException ignored) {
        }
        return Instant.now().plus(FALLBACK_TTL);
    }
}
