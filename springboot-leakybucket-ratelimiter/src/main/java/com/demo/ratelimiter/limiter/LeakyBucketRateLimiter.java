package com.demo.ratelimiter.limiter;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class LeakyBucketRateLimiter {

    private final int capacity;
    private final double leakRatePerSecond;
    private final Clock clock;

    private final Map<String, BucketState> states = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public LeakyBucketRateLimiter(int capacity, double leakRatePerSecond, Clock clock) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        if (leakRatePerSecond <= 0) throw new IllegalArgumentException("leakRatePerSecond must be > 0");
        this.capacity = capacity;
        this.leakRatePerSecond = leakRatePerSecond;
        this.clock = clock;
    }

    public RateLimitDecision tryAcquire(String key) {
        long nowMs = clock.millis();

        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            BucketState s = states.computeIfAbsent(key, k -> new BucketState(0.0, nowMs));

            // 1) Leak based on elapsed time
            long elapsedMs = nowMs - s.lastUpdatedMs;
            if (elapsedMs > 0) {
                double leaked = leakRatePerSecond * (elapsedMs / 1000.0);
                s.level = Math.max(0.0, s.level - leaked);
                s.lastUpdatedMs = nowMs;
            }

            // 2) Decide allow/deny
            double nextLevel = s.level + 1.0; // cost per request = 1
            if (nextLevel > capacity) {
                // How long until level drops enough to fit one more request?
                // Need: level - leakRate*t <= capacity-1  => t >= (level-(capacity-1))/leakRate
                double overflow = s.level - (capacity - 1.0);
                double seconds = overflow <= 0 ? 1.0 : (overflow / leakRatePerSecond);
                long retryAfter = Math.max(1L, (long) Math.ceil(seconds));

                return new RateLimitDecision(false, capacity, leakRatePerSecond, 0, retryAfter);
            }

            // Accept
            s.level = nextLevel;

            int remainingApprox = (int) Math.max(0, Math.floor(capacity - s.level));
            return new RateLimitDecision(true, capacity, leakRatePerSecond, remainingApprox, 0);

        } finally {
            lock.unlock();
        }
    }

    private static class BucketState {
        double level;          // current “water level”
        long lastUpdatedMs;    // last time we leaked
        BucketState(double level, long lastUpdatedMs) {
            this.level = level;
            this.lastUpdatedMs = lastUpdatedMs;
        }
    }
}