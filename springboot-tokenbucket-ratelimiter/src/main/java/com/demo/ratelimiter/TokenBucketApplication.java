package com.demo.ratelimiter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TokenBucketApplication {

    public static void main(String[] args) {
        SpringApplication.run(TokenBucketApplication.class, args);
    }
}
