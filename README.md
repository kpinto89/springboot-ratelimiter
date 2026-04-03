# springboot-ratelimiter

This workspace contains two Spring Boot examples that expose the same API with different rate-limiting strategies:

- `springboot-fixedwindow-ratelimiter`
- `springboot-tokenbucket-ratelimiter`

Both apps provide `GET /api/hello` and return HTTP `429 Too Many Requests` with body `Rate limit exceeded` when a limit is hit.

## Modules at a glance

| Module | Algorithm | Default limit behavior | Default port |
| --- | --- | --- | --- |
| `springboot-fixedwindow-ratelimiter` | Fixed Window | Up to 100 requests per client IP in a 60-second window | `8080` |
| `springboot-tokenbucket-ratelimiter` | Token Bucket | Bucket capacity 100, refills 100 tokens every 60 seconds per client IP | `8081` |

## springboot-fixedwindow-ratelimiter

### What it does

- Uses `FixedWindowRateLimiter` to count requests inside a fixed 60-second time window.
- Uses client IP (`request.getRemoteAddr()`) as the rate-limit key.
- Allows the first 100 requests in the active window; blocks additional requests until the next window.

### Config (`application.yml`)

- `server.port: 8080`
- `ratelimiter.fixed-window.max-requests: 100`
- `ratelimiter.fixed-window.window-size-millis: 60000`

## springboot-tokenbucket-ratelimiter

### What it does

- Uses `TokenBucketRateLimiter` with one bucket per client IP.
- Starts each client with 100 tokens.
- Consumes 1 token per request and refills 100 tokens every 60 seconds (up to capacity 100).

### Config (`application.yml`)

- `server.port: 8081`
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

Run token-bucket module:

```powershell
Set-Location "C:\Users\t_kevinpin\IdeaProjects\springboot-ratelimiter\springboot-tokenbucket-ratelimiter"
mvn spring-boot:run
```

Call endpoints:

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/hello" -UseBasicParsing
Invoke-WebRequest -Uri "http://localhost:8081/api/hello" -UseBasicParsing
```

## Tests

Run fixed-window tests:

```powershell
Set-Location "C:\Users\t_kevinpin\IdeaProjects\springboot-ratelimiter"
mvn -pl springboot-fixedwindow-ratelimiter test
```

Run token-bucket tests:

```powershell
Set-Location "C:\Users\t_kevinpin\IdeaProjects\springboot-ratelimiter"
mvn -pl springboot-tokenbucket-ratelimiter test
```
