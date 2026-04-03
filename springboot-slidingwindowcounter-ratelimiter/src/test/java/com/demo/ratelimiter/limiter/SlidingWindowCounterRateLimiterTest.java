package com.example.ratelimiter.limiter;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class SlidingWindowCounterRateLimiterTest {

    static class MutableClock extends Clock {
        private final AtomicLong nowMs;
        MutableClock(long startMs) { this.nowMs = new AtomicLong(startMs); }
        void setMs(long t) { nowMs.set(t); }
        void advanceMs(long delta) { nowMs.addAndGet(delta); }
        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public long millis() { return nowMs.get(); }
        @Override public Instant instant() { return Instant.ofEpochMilli(nowMs.get()); }
    }

    @Test
    void allowsUpToLimitInSameWindow_thenBlocks() {
        MutableClock clock = new MutableClock(0);
        SlidingWindowCounterRateLimiter rl = new SlidingWindowCounterRateLimiter(3, 1000, clock);

        assertTrue(rl.tryAcquire("k").allowed());
        assertTrue(rl.tryAcquire("k").allowed());
        assertTrue(rl.tryAcquire("k").allowed());
        assertFalse(rl.tryAcquire("k").allowed());
    }

    @Test
    void boundaryIsSmoothedUsingPreviousWindowWeight() {
        // windowMs = 1000
        MutableClock clock = new MutableClock(0);
        SlidingWindowCounterRateLimiter rl = new SlidingWindowCounterRateLimiter(4, 1000, clock);

        // End of window0: make 3 requests at t=900ms
        clock.setMs(900);
        assertTrue(rl.tryAcquire("k").allowed());
        assertTrue(rl.tryAcquire("k").allowed());
        assertTrue(rl.tryAcquire("k").allowed());

        // Enter next window at t=1000ms, but only 10ms in -> prev weight ~ 0.99
        clock.setMs(1010);

        // With 3 prev requests weighted ~2.97, only about 1 more should be allowed for limit=4
        assertTrue(rl.tryAcquire("k").allowed());
        assertFalse(rl.tryAcquire("k").allowed());
    }
}