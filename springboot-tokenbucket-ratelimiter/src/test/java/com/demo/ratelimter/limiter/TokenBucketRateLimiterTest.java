package com.demo.ratelimter.limiter;

import com.demo.ratelimiter.limter.TokenBucketRateLimiter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenBucketRateLimiterTest {

    @Test
    void shouldAllowRequestsUntilCapacityIsExhausted() {
        TokenBucketRateLimiter limiter =
                new TokenBucketRateLimiter(5, 5, 60_000);

        for (int i = 1; i <= 5; i++) {
            assertTrue(limiter.allowRequest(),
                    "Request " + i + " should be allowed");
        }

        // 6th request should be blocked
        assertFalse(limiter.allowRequest(),
                "Request beyond capacity should be blocked");
    }

    @Test
    void shouldRefillTokensAfterInterval() throws InterruptedException {
        TokenBucketRateLimiter limiter =
                new TokenBucketRateLimiter(2, 2, 100); // quick refill

        assertTrue(limiter.allowRequest());
        assertTrue(limiter.allowRequest());
        assertFalse(limiter.allowRequest()); // exhausted

        // wait for refill
        Thread.sleep(150);

        assertTrue(limiter.allowRequest(),
                "Request should be allowed after refill");
    }

    @Test
    void shouldNotExceedBucketCapacityAfterRefill() throws InterruptedException {
        TokenBucketRateLimiter limiter =
                new TokenBucketRateLimiter(3, 3, 100);

        Thread.sleep(200); // wait multiple refill cycles

        for (int i = 1; i <= 3; i++) {
            assertTrue(limiter.allowRequest());
        }

        assertFalse(limiter.allowRequest(),
                "Bucket should never exceed capacity");
    }
}
