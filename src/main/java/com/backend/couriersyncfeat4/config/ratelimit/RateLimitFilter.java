package com.backend.couriersyncfeat4.config.ratelimit;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rate limiting por IP (endpoints de auth sin sesión) y por usuario (GraphQL y
 * resto, usando el {@code SecurityContext} poblado por el JwtAuthenticationFilter).
 * {@code /events} (SSE) se excluye: es una conexión larga y se limita por
 * "una conexión por usuario" en {@code SseEmitterService}.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;
    private final RateLimitBucketProvider bucketProvider;

    public RateLimitFilter(RateLimitProperties properties, RateLimitBucketProvider bucketProvider) {
        this.properties = properties;
        this.bucketProvider = bucketProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (!properties.isEnabled()) {
            chain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (path.startsWith("/events")) {
            chain.doFilter(request, response);
            return;
        }

        String key;
        long capacity;

        if (path.startsWith("/auth/login") || path.startsWith("/auth/register")) {
            key = "auth:ip:" + clientIp(request);
            capacity = properties.getAuthCapacity();
        } else {
            String email = currentUserEmail();
            key = email != null ? "user:" + email : "ip:" + clientIp(request);
            capacity = properties.getApiCapacity();
        }

        Bucket bucket = bucketProvider.resolve(key, capacity, properties.getRefillDuration());
        if (!bucket.tryConsume(1)) {
            writeTooManyRequests(response);
            return;
        }
        chain.doFilter(request, response);
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"code\":\"TOO_MANY_REQUESTS\",\"status\":429,\"message\":\"Too many requests\"}");
    }
}
