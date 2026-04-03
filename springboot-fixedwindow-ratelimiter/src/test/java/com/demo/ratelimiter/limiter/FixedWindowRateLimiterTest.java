package com.demo.ratelimiter.limiter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedWindowRateLimiterTest {

    @Test
    void shouldAllowRequestsUntilLimitIsReached() {
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(3, 1_000);

        assertTrue(limiter.allowRequest("client-a"));
        assertTrue(limiter.allowRequest("client-a"));
        assertTrue(limiter.allowRequest("client-a"));
        assertFalse(limiter.allowRequest("client-a"));
    }

    @Test
    void shouldResetCounterWhenWindowExpires() throws InterruptedException {
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(2, 100);

        assertTrue(limiter.allowRequest("client-a"));
        assertTrue(limiter.allowRequest("client-a"));
        assertFalse(limiter.allowRequest("client-a"));

        Thread.sleep(150);

        assertTrue(limiter.allowRequest("client-a"));
        assertTrue(limiter.allowRequest("client-a"));
        assertFalse(limiter.allowRequest("client-a"));
    }

    @Test
    void shouldTrackEachClientInSeparateWindow() {
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(2, 1_000);

        assertTrue(limiter.allowRequest("client-a"));
        assertTrue(limiter.allowRequest("client-a"));
        assertFalse(limiter.allowRequest("client-a"));

        // A different client should still have its own quota.
        assertTrue(limiter.allowRequest("client-b"));
        assertTrue(limiter.allowRequest("client-b"));
        assertFalse(limiter.allowRequest("client-b"));
    }
}

