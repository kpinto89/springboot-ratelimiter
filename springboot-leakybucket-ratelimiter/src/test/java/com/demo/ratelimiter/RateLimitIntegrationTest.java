package com.demo.ratelimiter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "rate-limiter.leaky-bucket.enabled=true",
        "rate-limiter.leaky-bucket.capacity=2",
        "rate-limiter.leaky-bucket.leak-rate-per-second=0.1",
        "rate-limiter.leaky-bucket.key-type=HEADER",
        "rate-limiter.leaky-bucket.header-name=X-Api-Key",
        "rate-limiter.leaky-bucket.include-headers=true"
})
class RateLimitIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturn429AndHeadersWhenLeakyBucketIsFull() {
        HttpEntity<Void> request = requestWithApiKey("leaky-limit-test");

        ResponseEntity<String> first = restTemplate.exchange("/api/hello", HttpMethod.GET, request, String.class);
        ResponseEntity<String> second = restTemplate.exchange("/api/hello", HttpMethod.GET, request, String.class);
        ResponseEntity<String> third = restTemplate.exchange("/api/hello", HttpMethod.GET, request, String.class);

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(first.getBody()).isEqualTo("✅ allowed");
        assertThat(first.getHeaders().getFirst("X-RateLimit-Capacity")).isEqualTo("2");
        assertThat(first.getHeaders().getFirst("X-RateLimit-LeakRate")).isEqualTo("0.1");

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(second.getHeaders().getFirst("X-RateLimit-Remaining")).isEqualTo("0");

        assertThat(third.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(third.getBody()).isEqualTo("Rate limit exceeded");
        assertThat(third.getHeaders().getFirst("Retry-After")).isNotBlank();
        assertThat(Long.parseLong(third.getHeaders().getFirst("Retry-After"))).isGreaterThanOrEqualTo(1L);
    }

    @Test
    void shouldTrackDifferentHeaderKeysIndependently() {
        HttpEntity<Void> keyARequest = requestWithApiKey("client-a");
        HttpEntity<Void> keyBRequest = requestWithApiKey("client-b");

        ResponseEntity<String> aFirst = restTemplate.exchange("/api/hello", HttpMethod.GET, keyARequest, String.class);
        ResponseEntity<String> aSecond = restTemplate.exchange("/api/hello", HttpMethod.GET, keyARequest, String.class);
        ResponseEntity<String> bFirst = restTemplate.exchange("/api/hello", HttpMethod.GET, keyBRequest, String.class);

        assertThat(aFirst.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(aSecond.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(bFirst.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(bFirst.getHeaders().getFirst("X-RateLimit-Remaining")).isEqualTo("1");
    }

    private static HttpEntity<Void> requestWithApiKey(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", apiKey);
        return new HttpEntity<>(headers);
    }
}

