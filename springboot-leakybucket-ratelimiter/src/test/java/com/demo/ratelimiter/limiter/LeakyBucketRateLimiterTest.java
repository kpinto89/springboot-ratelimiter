package com.demo.ratelimiter.limiter;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class LeakyBucketRateLimiterTest {

    @Test
    void shouldAllowUntilCapacityThenBlock() {
        MutableClock clock = new MutableClock(Instant.ofEpochMilli(1_000));
        LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(2, 1.0, clock);

        RateLimitDecision first = limiter.tryAcquire("client-a");
        RateLimitDecision second = limiter.tryAcquire("client-a");
        RateLimitDecision third = limiter.tryAcquire("client-a");

        assertThat(first.allowed()).isTrue();
        assertThat(first.remainingApprox()).isEqualTo(1);
        assertThat(second.allowed()).isTrue();
        assertThat(second.remainingApprox()).isEqualTo(0);

        assertThat(third.allowed()).isFalse();
        assertThat(third.retryAfterSeconds()).isGreaterThanOrEqualTo(1L);
    }

    @Test
    void shouldLeakOverTimeAndAllowAgain() {
        MutableClock clock = new MutableClock(Instant.ofEpochMilli(1_000));
        LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(2, 1.0, clock);

        assertThat(limiter.tryAcquire("client-a").allowed()).isTrue();
        assertThat(limiter.tryAcquire("client-a").allowed()).isTrue();
        assertThat(limiter.tryAcquire("client-a").allowed()).isFalse();

        clock.advanceMillis(1_000);

        RateLimitDecision decision = limiter.tryAcquire("client-a");
        assertThat(decision.allowed()).isTrue();
        assertThat(decision.remainingApprox()).isEqualTo(0);
    }

    @Test
    void shouldUseIndependentBucketsPerKey() {
        MutableClock clock = new MutableClock(Instant.ofEpochMilli(1_000));
        LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(1, 1.0, clock);

        RateLimitDecision a1 = limiter.tryAcquire("client-a");
        RateLimitDecision a2 = limiter.tryAcquire("client-a");
        RateLimitDecision b1 = limiter.tryAcquire("client-b");

        assertThat(a1.allowed()).isTrue();
        assertThat(a2.allowed()).isFalse();
        assertThat(b1.allowed()).isTrue();
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void advanceMillis(long millis) {
            instant = instant.plusMillis(millis);
        }
    }
}

