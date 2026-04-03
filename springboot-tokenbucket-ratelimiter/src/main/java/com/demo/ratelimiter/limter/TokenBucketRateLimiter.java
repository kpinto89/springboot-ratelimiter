package com.demo.ratelimiter.limter;


import java.time.Instant;

public class TokenBucketRateLimiter {

    private final int capacity;
    private final int refillTokens;
    private final long refillIntervalMillis;

    private int tokens;
    private long lastRefillTime;

    public TokenBucketRateLimiter(int capacity, int refillTokens, long refillIntervalMillis) {
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillIntervalMillis = refillIntervalMillis;
        this.tokens = capacity;
        this.lastRefillTime = Instant.now().toEpochMilli();
    }

    public synchronized boolean allowRequest() {
        refill();

        if (tokens > 0) {
            tokens--;
            return true;
        }
        return false;
    }

    private void refill() {
        long now = Instant.now().toEpochMilli();
        long elapsed = now - lastRefillTime;

        if (elapsed >= refillIntervalMillis) {
            long intervals = elapsed / refillIntervalMillis;
            int newTokens = (int) (intervals * refillTokens);

            tokens = Math.min(capacity, tokens + newTokens);
            lastRefillTime += intervals * refillIntervalMillis;
        }
    }
}
