package com.demo.ratelimiter.limiter;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FixedWindowRateLimiter {

    private final long windowSizeMillis;
    private final int maxRequests;

    private final Map<String, Window> store = new ConcurrentHashMap<>();

    public FixedWindowRateLimiter(int maxRequests, long windowSizeMillis) {
        this.maxRequests = maxRequests;
        this.windowSizeMillis = windowSizeMillis;
    }

    public boolean allowRequest(String key) {
        long now = Instant.now().toEpochMilli();

        Window window = store.compute(key, (k, existing) -> {
            if (existing == null || now > existing.windowEnd) {
                return new Window(1, now + windowSizeMillis);
            }
            existing.counter++;
            return existing;
        });

        return window.counter <= maxRequests;
    }

    private static class Window {
        int counter;
        long windowEnd;

        Window(int counter, long windowEnd) {
            this.counter = counter;
            this.windowEnd = windowEnd;
        }
    }
}