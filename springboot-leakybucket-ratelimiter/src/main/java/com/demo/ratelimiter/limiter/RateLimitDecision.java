package com.demo.ratelimiter.limiter;

public record RateLimitDecision(
        boolean allowed,
        int capacity,
        double leakRatePerSecond,
        int remainingApprox,
        long retryAfterSeconds
) {}