package com.demo.ratelimiter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.profiles.active=test"})
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