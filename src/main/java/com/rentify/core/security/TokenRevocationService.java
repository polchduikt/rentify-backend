package com.rentify.core.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenRevocationService {

    private static final String REVOKED_JTI_KEY_PREFIX = "auth:revoked:jti:";

    private final JwtService jwtService;
    private final StringRedisTemplate redisTemplate;

    @Value("${application.security.jwt.revocation.enabled:true}")
    private boolean revocationEnabled;

    public void revoke(String token) {
        if (!revocationEnabled || token == null || token.isBlank()) {
            return;
        }

        String jti = resolveJti(token);
        if (jti == null) {
            return;
        }

        Instant expiresAt = resolveExpiration(token);
        if (expiresAt == null) {
            return;
        }

        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isNegative() || ttl.isZero()) {
            return;
        }

        try {
            redisTemplate.opsForValue().set(revokedTokenKey(jti), "1", ttl);
            log.info("Token revoked in Redis. JTI: {}, TTL: {} seconds", jti, ttl.getSeconds());
        } catch (RuntimeException ex) {
            log.warn("Failed to write revoked token jti to Redis: {}", ex.getMessage());
        }
    }


    private String resolveJti(String token) {
        try {
            String jti = jwtService.extractJti(token);
            if (jti != null && !jti.isBlank()) {
                return jti;
            }
        } catch (RuntimeException ignored) {
        }
        return null;
    }

    private Instant resolveExpiration(String token) {
        try {
            Date expiration = jwtService.extractExpiration(token);
            return expiration != null ? expiration.toInstant() : null;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public boolean isRevokedByJti(String jti) {
        if (!revocationEnabled || jti == null || jti.isBlank()) {
            return false;
        }
        try {
            Boolean isRevoked = Boolean.TRUE.equals(redisTemplate.hasKey(revokedTokenKey(jti)));
            log.debug("Token revocation check by JTI: {}, isRevoked: {}", jti, isRevoked);
            return isRevoked;
        } catch (RuntimeException ex) {
            log.warn("Failed to read revoked token jti from Redis: {}", ex.getMessage());
            return false;
        }
    }

    private String revokedTokenKey(String jti) {
        return REVOKED_JTI_KEY_PREFIX + jti;
    }
}
