package com.example.ratelimiter.limiter;

public record RateLimitDecision(
        boolean allowed,
        int limit,
        int remaining,
        long resetEpochSeconds
) {}
