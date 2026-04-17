package com.rentify.core.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Locale;

@Configuration
@RequiredArgsConstructor
public class HttpCacheConfig implements WebMvcConfigurer {

    private static final String NO_STORE = "no-store";

    private final CacheProperties cacheProperties;

    @Bean
    public FilterRegistrationBean<ShallowEtagHeaderFilter> apiEtagFilter() {
        ShallowEtagHeaderFilter filter = new ShallowEtagHeaderFilter();
        filter.setWriteWeakETag(true);

        FilterRegistrationBean<ShallowEtagHeaderFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setName("apiEtagFilter");
        registration.addUrlPatterns(
                "/api/v1/properties",
                "/api/v1/properties/*",
                "/api/v1/amenities",
                "/api/v1/amenities/*",
                "/api/v1/locations",
                "/api/v1/locations/*"
        );
        return registration;
    }

    @Bean
    public HandlerInterceptor apiCacheHeadersInterceptor() {
        return new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                if (response.getHeader(HttpHeaders.CACHE_CONTROL) != null) {
                    return true;
                }

                if (!isSafeMethod(request.getMethod())) {
                    response.setHeader(HttpHeaders.CACHE_CONTROL, NO_STORE);
                    appendVary(response, "Origin");
                    return true;
                }

                String path = normalizePath(request);
                response.setHeader(HttpHeaders.CACHE_CONTROL, resolveCacheControlValue(path));
                appendVary(response, "Origin");
                appendVary(response, "Accept-Encoding");
                return true;
            }
        };
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiCacheHeadersInterceptor())
                .addPathPatterns("/api/v1/**");
    }

    private static boolean isSafeMethod(String method) {
        if (method == null) {
            return false;
        }

        String normalized = method.toUpperCase(Locale.ROOT);
        return "GET".equals(normalized) || "HEAD".equals(normalized);
    }

    private static String normalizePath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath == null || contextPath.isBlank()) {
            return path;
        }
        return path.startsWith(contextPath) ? path.substring(contextPath.length()) : path;
    }

    private String resolveCacheControlValue(String path) {
        if (path.startsWith("/api/v1/amenities") || path.startsWith("/api/v1/locations")) {
            return publicMediumCacheControl();
        }

        if (path.startsWith("/api/v1/properties/") && path.endsWith("/reviews")) {
            return publicShortCacheControl();
        }

        if (path.matches("^/api/v1/users/\\d+$")) {
            return publicShortCacheControl();
        }

        if (path.startsWith("/api/v1/properties/me")) {
            return NO_STORE;
        }

        if (path.startsWith("/api/v1/properties")) {
            return publicShortCacheControl();
        }

        if (isPrivatePath(path)) {
            return NO_STORE;
        }

        return publicShortCacheControl();
    }

    private static boolean isPrivatePath(String path) {
        List<String> privatePrefixes = List.of(
                "/api/v1/sessions",
                "/api/v1/users",
                "/api/v1/bookings",
                "/api/v1/conversations",
                "/api/v1/favorites",
                "/api/v1/payments",
                "/api/v1/wallet",
                "/api/v1/promotion-packages",
                "/api/v1/subscriptions",
                "/api/v1/admin"
        );

        return privatePrefixes.stream().anyMatch(path::startsWith);
    }

    private static void appendVary(HttpServletResponse response, String value) {
        String current = response.getHeader(HttpHeaders.VARY);
        if (current == null || current.isBlank()) {
            response.setHeader(HttpHeaders.VARY, value);
            return;
        }

        if (!containsVaryToken(current, value)) {
            response.setHeader(HttpHeaders.VARY, current + ", " + value);
        }
    }

    private static boolean containsVaryToken(String current, String token) {
        String target = token.toLowerCase(Locale.ROOT);
        return List.of(current.split(",")).stream()
                .map(String::trim)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(target::equals);
    }

    private String publicShortCacheControl() {
        return buildPublicCacheControl(cacheProperties.getShortMaxAge(), cacheProperties.getShortSwr());
    }

    private String publicMediumCacheControl() {
        return buildPublicCacheControl(cacheProperties.getMediumMaxAge(), cacheProperties.getMediumSwr());
    }

    private String buildPublicCacheControl(long maxAgeSeconds, long swrSeconds) {
        return "public, max-age=" + maxAgeSeconds + ", stale-while-revalidate=" + swrSeconds;
    }
}
