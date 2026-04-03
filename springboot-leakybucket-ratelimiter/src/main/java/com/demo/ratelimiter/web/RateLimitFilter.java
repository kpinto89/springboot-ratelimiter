package com.demo.ratelimiter.web;

import com.demo.ratelimiter.config.LeakyBucketProperties;
import com.demo.ratelimiter.limiter.LeakyBucketRateLimiter;
import com.demo.ratelimiter.limiter.RateLimitDecision;
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

    private final LeakyBucketProperties props;
    private final LeakyBucketRateLimiter limiter;

    public RateLimitFilter(LeakyBucketProperties props) {
        this.props = props;
        this.limiter = new LeakyBucketRateLimiter(
                props.getCapacity(),
                props.getLeakRatePerSecond(),
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
        RateLimitDecision d = limiter.tryAcquire(key);

        if (props.isIncludeHeaders()) {
            response.setHeader("X-RateLimit-Capacity", String.valueOf(d.capacity()));
            response.setHeader("X-RateLimit-LeakRate", String.valueOf(d.leakRatePerSecond()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(d.remainingApprox()));
        }

        if (!d.allowed()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(d.retryAfterSeconds()));
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
        return request.getRemoteAddr();
    }
}