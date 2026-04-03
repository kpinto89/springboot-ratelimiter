package com.demo.ratelimiter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "rate-limiter.sliding-window-counter.limit=5",
                "rate-limiter.sliding-window-counter.window-ms=60000",
                "rate-limiter.sliding-window-counter.enabled=true",
                "rate-limiter.sliding-window-counter.key-type=IP"
        })
class RateLimitIntegrationTest {

    @Autowired
    TestRestTemplate rest;

    @Test
    void returns429AfterLimitExceeded() {
        for (int i = 0; i < 5; i++) {
            assertThat(rest.getForEntity("/api/hello", String.class).getStatusCode())
                    .isEqualTo(HttpStatus.OK);
        }
        assertThat(rest.getForEntity("/api/hello", String.class).getStatusCode())
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
}