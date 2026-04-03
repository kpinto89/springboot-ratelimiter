package com.demo.ratelimiter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RateLimitIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturn429AfterFixedWindowLimitIsExceeded() {
        ResponseEntity<String> response = null;

        for (int i = 0; i < 100; i++) {
            response = restTemplate.getForEntity("/api/hello", String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        response = restTemplate.getForEntity("/api/hello", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody()).isEqualTo("Rate limit exceeded");
    }
}

