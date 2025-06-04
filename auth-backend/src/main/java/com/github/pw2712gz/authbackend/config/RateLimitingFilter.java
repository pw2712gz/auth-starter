package com.github.pw2712gz.authbackend.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basic in-memory rate limiting filter for sensitive auth endpoints.
 * Limits each IP to a max of 10 requests per minute.
 */
@Slf4j
public class RateLimitingFilter implements Filter {

    private static final int MAX_REQUESTS = 10;
    private static final long TIME_WINDOW_MS = 60_000;

    private static final Set<String> RATE_LIMITED_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password",
            "/api/auth/reset-password"
    );

    private final Map<String, RequestInfo> requestCounts = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        String ip = request.getRemoteAddr();
        String path = httpReq.getRequestURI();
        long now = Instant.now().toEpochMilli();

        if (!RATE_LIMITED_PATHS.contains(path)) {
            chain.doFilter(request, response);
            return;
        }

        RequestInfo info = requestCounts.computeIfAbsent(ip, k -> new RequestInfo(0, now));

        synchronized (info) {
            if (now - info.timestamp > TIME_WINDOW_MS) {
                info.count = 1;
                info.timestamp = now;
            } else {
                info.count++;
            }

            if (info.count > MAX_REQUESTS) {
                log.warn("[RateLimit] IP {} exceeded limit on {} ({} requests)", ip, path, info.count);
                httpResp.setStatus(429);
                httpResp.setContentType("application/json");
                httpResp.getWriter().write("""
                        {
                            "status": 429,
                            "error": "Too Many Requests",
                            "message": "Too many requests. Please try again later."
                        }
                        """);
                return;
            }
        }

        cleanupOldEntries(now);
        chain.doFilter(request, response);
    }

    /**
     * Removes entries older than twice the rate limit window.
     */
    private void cleanupOldEntries(long now) {
        requestCounts.entrySet().removeIf(entry ->
                now - entry.getValue().timestamp > 2 * TIME_WINDOW_MS
        );
    }

    /**
     * Tracks request count and window start time per IP.
     */
    private static class RequestInfo {
        int count;
        long timestamp;

        RequestInfo(int count, long timestamp) {
            this.count = count;
            this.timestamp = timestamp;
        }
    }
}
