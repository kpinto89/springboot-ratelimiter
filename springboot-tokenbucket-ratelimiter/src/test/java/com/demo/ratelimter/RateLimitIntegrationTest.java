package com.demo.ratelimter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RateLimitIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturn429AfterRateLimitIsExceeded() {

        ResponseEntity<String> response = null;

        // Default limiter: 100 requests
        for (int i = 1; i <= 100; i++) {
            response = restTemplate.getForEntity("/api/hello", String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        // Exceed limit
        response = restTemplate.getForEntity("/api/hello", String.class);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
}
