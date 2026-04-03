package com.demo.ratelimiter.limiter;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class SlidingWindowCounterRateLimiter {

    private final int limit;
    private final long windowMs;
    private final Clock clock;

    private final Map<String, WindowState> states = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public SlidingWindowCounterRateLimiter(int limit, long windowMs, Clock clock) {
        this.limit = limit;
        this.windowMs = windowMs;
        this.clock = clock;
    }

    public RateLimitDecision tryAcquire(String key) {
        long now = clock.millis();
        long currentWindowStart = (now / windowMs) * windowMs;
        long prevWindowStart = currentWindowStart - windowMs;

        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            WindowState s = states.computeIfAbsent(key, k -> new WindowState(currentWindowStart));

            // Rotate windows if needed
            if (s.windowStart == currentWindowStart) {
                // same window
            } else if (s.windowStart == prevWindowStart) {
                // moved exactly one window ahead: shift counts
                s.prevCount = s.currCount;
                s.currCount = 0;
                s.windowStart = currentWindowStart;
            } else {
                // jumped more than 1 window: reset
                s.prevCount = 0;
                s.currCount = 0;
                s.windowStart = currentWindowStart;
            }

            long timeIntoCurrent = now - s.windowStart;
            double weight = (double) (windowMs - timeIntoCurrent) / windowMs; // [0..1]
            double effective = s.currCount + (s.prevCount * weight);

            if (effective >= limit) {
                long resetEpochSeconds = (s.windowStart + windowMs) / 1000;
                return new RateLimitDecision(false, limit, 0, resetEpochSeconds);
            }

            s.currCount++;
            double effectiveAfter = s.currCount + (s.prevCount * weight);
            int remaining = Math.max(0, (int) Math.floor(limit - effectiveAfter));

            long resetEpochSeconds = (s.windowStart + windowMs) / 1000;
            return new RateLimitDecision(true, limit, remaining, resetEpochSeconds);
        } finally {
            lock.unlock();
        }
    }

    private static class WindowState {
        long windowStart;
        int prevCount;
        int currCount;

        WindowState(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}