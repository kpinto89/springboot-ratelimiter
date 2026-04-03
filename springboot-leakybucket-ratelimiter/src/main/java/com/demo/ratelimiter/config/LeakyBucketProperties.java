package com.demo.ratelimiter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rate-limiter.leaky-bucket")
public class LeakyBucketProperties {
    private boolean enabled = true;
    private int capacity = 10;
    private double leakRatePerSecond = 2.0;
    private String keyType = "IP";       // IP | HEADER
    private String headerName = "X-Api-Key";
    private boolean includeHeaders = true;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public double getLeakRatePerSecond() { return leakRatePerSecond; }
    public void setLeakRatePerSecond(double leakRatePerSecond) { this.leakRatePerSecond = leakRatePerSecond; }

    public String getKeyType() { return keyType; }
    public void setKeyType(String keyType) { this.keyType = keyType; }

    public String getHeaderName() { return headerName; }
    public void setHeaderName(String headerName) { this.headerName = headerName; }

    public boolean isIncludeHeaders() { return includeHeaders; }
    public void setIncludeHeaders(boolean includeHeaders) { this.includeHeaders = includeHeaders; }
}