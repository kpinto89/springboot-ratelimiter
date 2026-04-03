package com.demo.ratelimiter.filter;

import com.demo.ratelimiter.limter.TokenBucketRateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, TokenBucketRateLimiter> buckets = new ConcurrentHashMap<>();

    private TokenBucketRateLimiter resolveBucket(String ip) {
        return buckets.computeIfAbsent(ip,
                k -> new TokenBucketRateLimiter(100, 100, 60_000));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();

        TokenBucketRateLimiter limiter = resolveBucket(clientIp);

        if (!limiter.allowRequest()) {
            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            response.getWriter().write("Rate limit exceeded");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
