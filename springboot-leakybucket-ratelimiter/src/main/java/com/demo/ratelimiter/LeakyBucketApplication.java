package com.demo.ratelimiter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LeakyBucketApplication {
    public static void main(String[] args) {
        SpringApplication.run(LeakyBucketApplication.class, args);
    }
}