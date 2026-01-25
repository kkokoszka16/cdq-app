package com.banking.infrastructure.adapter.in.web.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter using Bucket4j.
 * Limits requests per client IP address.
 */
@Slf4j
@Component
public class RateLimitFilter implements Filter {

    private static final String TOO_MANY_REQUESTS_MESSAGE = "Rate limit exceeded. Please try again later.";

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int requestsPerMinute;

    public RateLimitFilter(
            @Value("${rate-limit.requests-per-minute:100}") int requestsPerMinute
    ) {
        this.requestsPerMinute = requestsPerMinute;
    }

    @Override
    public void doFilter(
            ServletRequest servletRequest,
            ServletResponse servletResponse,
            FilterChain chain
    ) throws IOException, ServletException {
        var request = (HttpServletRequest) servletRequest;
        var response = (HttpServletResponse) servletResponse;

        var clientIp = extractClientIp(request);
        var bucket = resolveBucket(clientIp);

        if (bucket.tryConsume(1)) {
            chain.doFilter(servletRequest, servletResponse);
        } else {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            sendRateLimitResponse(response);
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        var xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    private Bucket resolveBucket(String clientIp) {
        return buckets.computeIfAbsent(clientIp, this::createBucket);
    }

    private Bucket createBucket(String clientIp) {
        var limit = Bandwidth.builder()
                .capacity(requestsPerMinute)
                .refillGreedy(requestsPerMinute, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private void sendRateLimitResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"type\":\"about:blank\",\"title\":\"Too Many Requests\",\"status\":429,\"detail\":\""
                + TOO_MANY_REQUESTS_MESSAGE + "\"}"
        );
    }
}
