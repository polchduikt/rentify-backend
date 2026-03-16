package com.rentify.core.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthCookieService {

    private static final String COOKIE_STRATEGY = "cookie";

    @Value("${application.security.auth.strategy:bearer}")
    private String authStrategy;

    @Value("${application.security.auth.cookie-name:rentify_access_token}")
    private String accessTokenCookieName;

    @Value("${application.security.auth.cookie-path:/}")
    private String cookiePath;

    @Value("${application.security.auth.cookie-domain:}")
    private String cookieDomain;

    @Value("${application.security.auth.cookie-secure:true}")
    private boolean cookieSecure;

    @Value("${application.security.auth.cookie-http-only:true}")
    private boolean cookieHttpOnly;

    @Value("${application.security.auth.cookie-same-site:Lax}")
    private String cookieSameSite;

    @Value("${application.security.jwt.expiration:86400000}")
    private long jwtExpirationMs;

    public boolean isCookieStrategyEnabled() {
        if (authStrategy == null) {
            return false;
        }
        return COOKIE_STRATEGY.equalsIgnoreCase(authStrategy.trim());
    }

    public String getAccessTokenCookieName() {
        return accessTokenCookieName;
    }

    public void writeAccessTokenCookie(HttpServletResponse response, String token) {
        if (!isCookieStrategyEnabled() || token == null || token.isBlank()) {
            return;
        }

        long expirationMs = Math.max(jwtExpirationMs, 1L);
        ResponseCookie cookie = baseCookieBuilder(token)
                .maxAge(Duration.ofMillis(expirationMs))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clearAccessTokenCookie(HttpServletResponse response) {
        if (!isCookieStrategyEnabled()) {
            return;
        }

        ResponseCookie cookie = baseCookieBuilder("")
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (!accessTokenCookieName.equals(cookie.getName())) {
                continue;
            }

            String value = cookie.getValue();
            if (value == null || value.isBlank()) {
                return null;
            }
            return value;
        }
        return null;
    }

    private ResponseCookie.ResponseCookieBuilder baseCookieBuilder(String value) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(accessTokenCookieName, value)
                .path(cookiePath)
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .sameSite(cookieSameSite);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }

        return builder;
    }
}
