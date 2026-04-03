package com.demo.ratelimiter.web;

import com.demo.ratelimiter.config.RateLimiterProperties;
import com.demo.ratelimiter.limiter.RateLimitDecision;
import com.demo.ratelimiter.limiter.SlidingWindowLogRateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterProperties props;
    private final SlidingWindowLogRateLimiter limiter;

    public RateLimitFilter(RateLimiterProperties props) {
        this.props = props;
        this.limiter = new SlidingWindowLogRateLimiter(
                props.getLimit(),
                props.getWindowMs(),
                Clock.systemUTC()
        );
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!props.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = resolveKey(request);
        RateLimitDecision decision = limiter.tryAcquire(key);

        if (props.isIncludeHeaders()) {
            response.setHeader("X-RateLimit-Limit", String.valueOf(decision.limit()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(decision.remaining()));
            response.setHeader("X-RateLimit-Reset", String.valueOf(decision.resetEpochSeconds()));
        }

        if (!decision.allowed()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(decision.retryAfterSeconds()));
            response.getWriter().write("Rate limit exceeded");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveKey(HttpServletRequest request) {
        String type = props.getKeyType() == null ? "IP" : props.getKeyType().trim().toUpperCase();
        if ("HEADER".equals(type)) {
            String v = request.getHeader(props.getHeaderName());
            return (v == null || v.isBlank()) ? "anonymous" : v;
        }
        // default IP
        return request.getRemoteAddr();
    }
}
