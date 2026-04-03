package com.demo.ratelimiter.limiter;

public record RateLimitDecision(
        boolean allowed,
        int limit,
        int remaining,
        long resetEpochSeconds
) {}
