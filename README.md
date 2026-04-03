# springboot-ratelimiter

This workspace contains four Spring Boot examples that expose the same API with different rate-limiting strategies:

- `springboot-fixedwindow-ratelimiter`
- `springboot-slidingwindowlog-ratelimiter`
- `springboot-slidingwindowcounter-ratelimiter`
- `springboot-tokenbucket-ratelimiter`

All apps provide `GET /api/hello` and return HTTP `429 Too Many Requests` with body `Rate limit exceeded` when a limit is hit.

## Modules at a glance

| Module | Algorithm | Default limit behavior | Default port |
| --- | --- | --- | --- |
| `springboot-fixedwindow-ratelimiter` | Fixed Window | Up to 100 requests per client IP in a 60-second window | `8080` |
| `springboot-slidingwindowlog-ratelimiter` | Sliding Window Log | Up to 100 requests per client key during the last rolling 60 seconds | `8081` |
| `springboot-slidingwindowcounter-ratelimiter` | Sliding Window Counter | Up to 100 requests per client key using a weighted two-window approximation | `8083` |
| `springboot-tokenbucket-ratelimiter` | Token Bucket | Bucket capacity 100, refills 100 tokens every 60 seconds per client IP | `8082` |

## springboot-fixedwindow-ratelimiter

### What it does

- Uses `FixedWindowRateLimiter` to count requests inside a fixed 60-second time window.
- Uses client IP (`request.getRemoteAddr()`) as the rate-limit key.
- Allows the first 100 requests in the active window; blocks additional requests until the next window.

### Config (`application.yml`)

- `server.port: 8080`
- `ratelimiter.fixed-window.max-requests: 100`
- `ratelimiter.fixed-window.window-size-millis: 60000`

## springboot-slidingwindowlog-ratelimiter

### What it does

- Uses `SlidingWindowLogRateLimiter` to store request timestamps per client and evaluate a rolling 60-second window.
- Supports rate-limit keys by client IP or request header via `rate-limiter.sliding-window-log.key-type`.
- Adds `X-RateLimit-*` headers when enabled and rejects over-limit requests with HTTP `429`.

### Config (`application.yml`)

- `server.port: 8081`
- `rate-limiter.sliding-window-log.enabled: true`
- `rate-limiter.sliding-window-log.limit: 100`
- `rate-limiter.sliding-window-log.window-ms: 60000`
- `rate-limiter.sliding-window-log.key-type: IP`
- `rate-limiter.sliding-window-log.header-name: X-Api-Key`
- `rate-limiter.sliding-window-log.include-headers: true`

## springboot-slidingwindowcounter-ratelimiter

### What it does

- Uses `SlidingWindowCounterRateLimiter` to approximate a rolling window by weighting the previous fixed window's count against elapsed time.
- More memory-efficient than the log variant: stores only two integer counters per key instead of all timestamps.
- Supports rate-limit keys by client IP or request header via `rate-limiter.sliding-window-counter.key-type`.
- Adds `X-RateLimit-*` headers when enabled and rejects over-limit requests with HTTP `429`.

### Config (`application.yml`)

- `server.port: 8083`
- `rate-limiter.sliding-window-counter.enabled: true`
- `rate-limiter.sliding-window-counter.limit: 100`
- `rate-limiter.sliding-window-counter.window-ms: 60000`
- `rate-limiter.sliding-window-counter.key-type: IP`
- `rate-limiter.sliding-window-counter.header-name: X-Api-Key`
- `rate-limiter.sliding-window-counter.include-headers: true`

## springboot-tokenbucket-ratelimiter

### What it does

- Uses `TokenBucketRateLimiter` with one bucket per client IP.
- Starts each client with 100 tokens.
- Consumes 1 token per request and refills 100 tokens every 60 seconds (up to capacity 100).

### Config (`application.yml`)

- `server.port: 8082`
- `ratelimiter.token-bucket.capacity: 100`
- `ratelimiter.token-bucket.refill-tokens: 100`
- `ratelimiter.token-bucket.refill-interval-millis: 60000`

## Quick start

> Prerequisite: Java 17+. Maven commands below require `mvn` to be installed and available in `PATH`.

Run fixed-window module:

```powershell
Set-Location "C:\Users\t_kevinpin\IdeaProjects\springboot-ratelimiter\springboot-fixedwindow-ratelimiter"
mvn spring-boot:run
```

Run sliding-window-log module:

```powershell
Set-Location "C:\Users\t_kevinpin\IdeaProjects\springboot-ratelimiter\springboot-slidingwindowlog-ratelimiter"
mvn spring-boot:run
```

Run sliding-window-counter module:

```powershell
Set-Location "C:\Users\t_kevinpin\IdeaProjects\springboot-ratelimiter\springboot-slidingwindowcounter-ratelimiter"
mvn spring-boot:run
```

Run token-bucket module:

```powershell
Set-Location "C:\Users\t_kevinpin\IdeaProjects\springboot-ratelimiter\springboot-tokenbucket-ratelimiter"
mvn spring-boot:run
```

Call endpoints:

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/hello" -UseBasicParsing
Invoke-WebRequest -Uri "http://localhost:8081/api/hello" -UseBasicParsing
Invoke-WebRequest -Uri "http://localhost:8082/api/hello" -UseBasicParsing
Invoke-WebRequest -Uri "http://localhost:8083/api/hello" -UseBasicParsing
```

## Tests

Run fixed-window tests:

```powershell
Set-Location "C:\Users\t_kevinpin\IdeaProjects\springboot-ratelimiter"
mvn -pl springboot-fixedwindow-ratelimiter test
```

Run sliding-window-log tests:

```powershell
Set-Location "C:\Users\t_kevinpin\IdeaProjects\springboot-ratelimiter"
mvn -pl springboot-slidingwindowlog-ratelimiter test
```

Run sliding-window-counter tests:

```powershell
Set-Location "C:\Users\t_kevinpin\IdeaProjects\springboot-ratelimiter"
mvn -pl springboot-slidingwindowcounter-ratelimiter test
```

Run token-bucket tests:

```powershell
Set-Location "C:\Users\t_kevinpin\IdeaProjects\springboot-ratelimiter"
mvn -pl springboot-tokenbucket-ratelimiter test
```
