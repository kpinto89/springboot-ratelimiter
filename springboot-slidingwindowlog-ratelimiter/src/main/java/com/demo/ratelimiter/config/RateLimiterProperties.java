package com.demo.ratelimiter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rate-limiter.sliding-window-log")
public class RateLimiterProperties {
    private boolean enabled = true;
    private int limit = 100;
    private long windowMs = 60_000;
    private String keyType = "IP";     // IP | HEADER
    private String headerName = "X-Api-Key";
    private boolean includeHeaders = true;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }

    public long getWindowMs() { return windowMs; }
    public void setWindowMs(long windowMs) { this.windowMs = windowMs; }

    public String getKeyType() { return keyType; }
    public void setKeyType(String keyType) { this.keyType = keyType; }

    public String getHeaderName() { return headerName; }
    public void setHeaderName(String headerName) { this.headerName = headerName; }

    public boolean isIncludeHeaders() { return includeHeaders; }
    public void setIncludeHeaders(boolean includeHeaders) { this.includeHeaders = includeHeaders; }
}
