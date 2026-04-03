package com.demo.ratelimiter.limiter;

import java.time.Clock;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class SlidingWindowLogRateLimiter {

    private final int limit;
    private final long windowMs;
    private final Clock clock;

    // timestamps per key
    private final Map<String, Deque<Long>> logs = new ConcurrentHashMap<>();
    // per-key locks to keep operations atomic
    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public SlidingWindowLogRateLimiter(int limit, long windowMs, Clock clock) {
        this.limit = limit;
        this.windowMs = windowMs;
        this.clock = clock;
    }

    public RateLimitDecision tryAcquire(String key) {
        long nowMs = clock.millis();
        long windowStart = nowMs - windowMs;

        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            Deque<Long> q = logs.computeIfAbsent(key, k -> new ArrayDeque<>());

            // prune old timestamps
            while (!q.isEmpty() && q.peekFirst() < windowStart) {
                q.pollFirst();
            }

            int used = q.size();
            if (used >= limit) {
                long oldest = q.peekFirst() != null ? q.peekFirst() : nowMs;
                long retryAfterMs = (oldest + windowMs) - nowMs;
                long retryAfterSec = Math.max(1, (retryAfterMs + 999) / 1000); // ceil, min 1s
                long resetSec = (oldest + windowMs) / 1000;

                return new RateLimitDecision(false, limit, 0, retryAfterSec, resetSec);
            }

            // accept and log timestamp
            q.addLast(nowMs);
            int remaining = Math.max(0, limit - (used + 1));

            // reset time is when oldest will expire; if only one item, it's itself
            long oldest = q.peekFirst() != null ? q.peekFirst() : nowMs;
            long resetSec = (oldest + windowMs) / 1000;

            return new RateLimitDecision(true, limit, remaining, 0, resetSec);

        } finally {
            lock.unlock();
        }
    }

    /** Optional: cleanup empty keys (call from a scheduled task if you want). */
    public void cleanupIdleKeys() {
        for (String key : logs.keySet()) {
            ReentrantLock lock = locks.get(key);
            if (lock == null) continue;

            if (lock.tryLock()) {
                try {
                    Deque<Long> q = logs.get(key);
                    if (q == null || q.isEmpty()) {
                        logs.remove(key);
                        locks.remove(key);
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}
