# BotBye Java Module

Java SDK for the [BotBye](https://botbye.com) Unified Protection Platform — unifying fraud prevention and real-time event monitoring in one platform.

BotBye goes beyond fixed bot/ATO checks. Risk dimensions and metrics are fully dynamic — you define what to measure and what rules to apply per project. This means the same platform covers bot detection, account takeover, multi-accounting, payment fraud, promotion abuse, or any custom fraud scenario specific to your business.

## Requirements

- Java 11 or higher
- Gradle or Maven

## Installation

### Gradle (Kotlin DSL)

```kotlin
implementation("com.botbye:java-module:2.1.0")
```

### Gradle (Groovy DSL)

```groovy
implementation 'com.botbye:java-module:2.1.0'
```

### Maven

```xml
<dependency>
    <groupId>com.botbye</groupId>
    <artifactId>java-module</artifactId>
    <version>2.1.0</version>
</dependency>
```

## Overview

The SDK provides three request types for different integration levels:

| Request Type | Use Case | Where It Runs |
|---|---|---|
| `BotbyeValidationEvent` | **Level 1** — Bot filtering | Proxy or middleware, before user identity is known |
| `BotbyeRiskScoringEvent` | **Level 2** — Risk scoring & event logging | Application layer, when user identity is known |
| `BotbyeFullEvent` | **Level 1+2 combined** | Application layer when no separate proxy exists |

All requests go to a single endpoint (`POST /api/v1/protect/evaluate`) and return a unified response with a decision (`ALLOW`, `CHALLENGE`, `BLOCK`), risk scores per dimension, and triggered signals. Dimensions are dynamic — the platform ships with built-in ones (`bot`, `ato`, `abuse`) but you can define custom dimensions (e.g., `payment_fraud`, `promotion_abuse`) per project without code changes.

Every evaluation call is also recorded as a **protection event** — logged to the analytics pipeline and used to compute real-time metrics that feed the rules engine. Metrics are fully configurable per project: the platform ships with built-in ones (failed logins, distinct IPs per account, device reuse, etc.) and you can define custom metrics for your specific use case (e.g., "failed transactions over $1000 per account in 1 hour"). This means `BotbyeRiskScoringEvent` serves a dual purpose: it both evaluates risk **and** logs the event for future analysis and metric aggregation.

## Quick Start

### 1. Initialize the Client

```java
import com.botbye.Botbye;
import com.botbye.model.BotbyeConfig;

BotbyeConfig config = new BotbyeConfig.Builder()
    .serverKey("your-server-key") // from https://app.botbye.com
    .build();

Botbye botbye = new Botbye(config);
```

### 2. Bot Validation (Level 1)

Validate device tokens where user identity is not yet available — at the proxy layer or in a middleware before authentication.

```java
import com.botbye.model.BotbyeValidationEvent;

Map<String, String> headers = flattenHeaders(request);

BotbyeEvaluateResponse response = botbye.evaluate(BotbyeValidationEvent.of(
    request.getRemoteAddr(),
    request.getParameter("botbye_token"), // extract the token from wherever you pass it: query param, header, body, etc.
    headers,
    request.getMethod(),
    request.getRequestURI(),
    Collections.emptyMap()
));

if (response.isBlocked()) {
    httpResponse.setStatus(403);
    return;
}
```

### 3. Risk Scoring & Event Logging (Level 2)

Evaluate risk and log events when user identity is known. Each call both scores the request **and** feeds the real-time metrics engine, so you should call `evaluate()` for every significant user action — not just when you need a decision.

```java
import com.botbye.model.BotbyeRiskScoringEvent;
import com.botbye.model.BotbyeUserInfo;
import com.botbye.model.BotbyeEventStatus;
import com.botbye.model.BotbyeDecision;

BotbyeEvaluateResponse response = botbye.evaluate(BotbyeRiskScoringEvent.of(
    request.getRemoteAddr(),
    headers,
    new BotbyeUserInfo(userId, null, userEmail, userPhone),
    "LOGIN",
    BotbyeEventStatus.SUCCESSFUL,
    request.getHeader("X-Botbye-Result"), // from Level 1
    Collections.emptyMap()
));

switch (response.getDecision()) {
    case BLOCK     -> { return ResponseEntity.status(403).build(); }
    case CHALLENGE -> { return showChallenge(response.getChallenge()); }
    case ALLOW     -> continueRequest();
}
```

When `botbyeResult` is `null` (no Level 1 upstream), bot validation is automatically bypassed.

#### Event Types

`eventType` is an arbitrary string — the server accepts any value. Pass any string that matches your business domain:

```java
"LOGIN"
"REGISTRATION"
"TRANSACTION"
"BONUS_CLAIM"
"PASSWORD_RESET"
"WITHDRAWAL"
```

#### Using Level 2 for Event Logging

Even when you don't need to act on the decision, sending events builds the metrics profile for the account. This enables rules like "more than 5 failed logins in 10 minutes" or "distinct devices per account in 1 hour":

```java
// Log a failed login attempt — feeds metrics even if you don't act on the decision
botbye.evaluate(BotbyeRiskScoringEvent.of(
    request.getRemoteAddr(),
    headers,
    new BotbyeUserInfo(userId),
    "LOGIN",
    BotbyeEventStatus.FAILED
));

// Log a custom business event
botbye.evaluate(BotbyeRiskScoringEvent.of(
    request.getRemoteAddr(),
    headers,
    new BotbyeUserInfo(userId),
    "BONUS_CLAIM",
    BotbyeEventStatus.SUCCESSFUL,
    null,
    Map.of("bonus_id", "welcome_100")
));
```

### 4. Full Evaluation (Level 1+2 Combined)

Use when there is no separate proxy layer — validates the device token and evaluates risk in a single call.

```java
import com.botbye.model.BotbyeFullEvent;

BotbyeEvaluateResponse response = botbye.evaluate(BotbyeFullEvent.of(
    request.getRemoteAddr(),
    request.getParameter("botbye_token"),
    headers,
    new BotbyeUserInfo(userId),
    "LOGIN",
    BotbyeEventStatus.FAILED
));
```

### Sync vs Async Evaluation

The `evaluate()` method is synchronous — it blocks the calling thread until the response is received:

```java
BotbyeEvaluateResponse response = botbye.evaluate(event);
```

For non-blocking evaluation, use `evaluateAsync()` which returns a `CompletableFuture`:

```java
CompletableFuture<BotbyeEvaluateResponse> future = botbye.evaluateAsync(event);
future.thenAccept(response -> {
    if (response.isBlocked()) {
        // handle blocked request
    }
});
```

## Response

`BotbyeEvaluateResponse` contains:

| Field | Type | Description |
|---|---|---|
| `requestId` | `UUID` | Request UUID |
| `decision` | `BotbyeDecision` | `ALLOW`, `CHALLENGE`, or `BLOCK` |
| `riskScore` | `Double` | Overall risk score (0–1) |
| `scores` | `Map<String, Double>` | Per-dimension scores (`bot`, `ato`, `abuse`, ...) |
| `signals` | `Set<String>` | Triggered signal names (e.g., `BruteForce`, `ImpossibleTravel`) |
| `challenge` | `BotbyeChallenge` | Challenge type and token (when decision is `CHALLENGE`) |
| `extraData` | `BotbyeExtraData` | Enriched device data (IP, country, browser, device, etc.) |
| `error` | `BotbyeError` | Error details (on fallback) |
| `botbyeResult` | `String` | Encoded result for Level 1→2 propagation |

```java
response.getDecision();              // BotbyeDecision.ALLOW
response.isBlocked();                // false
response.getRiskScore();             // 0.72
response.getScores();                // {bot=0.15, ato=0.72, abuse=0.05}
response.getSignals();               // [BruteForce, ImpossibleTravel]
response.getChallenge().getType();   // "captcha"
response.getExtraData().getCountry();// "US"
```

## Level 1 to Level 2 Propagation

When using both levels, propagate the Level 1 result to Level 2 via the `botbyeResult` field from the response. This allows the platform to link both evaluations by `requestId` and combine bot score from Level 1 with risk scores from Level 2 into a single unified result:

```java
// Level 1 (proxy) — validate and get result
BotbyeEvaluateResponse l1Response = botbye.evaluate(BotbyeValidationEvent.of(...));

// Pass botbyeResult to Level 2 (e.g. via header or directly)
BotbyeEvaluateResponse l2Response = botbye.evaluate(BotbyeRiskScoringEvent.of(
    // ...
    l1Response.getBotbyeResult(),
    Collections.emptyMap()
));
```

## Configuration

```java
BotbyeConfig config = new BotbyeConfig.Builder()
    .serverKey("your-server-key")                          // from https://app.botbye.com
    .botbyeEndpoint("https://verify.botbye.com")           // default
    .readTimeout(Duration.ofSeconds(2))                    // default
    .writeTimeout(Duration.ofSeconds(2))                   // default
    .connectionTimeout(Duration.ofSeconds(2))              // default
    .callTimeout(Duration.ofSeconds(5))                    // default
    .maxIdleConnections(250)                               // default
    .keepAliveDuration(Duration.ofSeconds(300))             // default
    .maxRequestsPerHost(1500)                              // default
    .maxRequests(1500)                                     // default
    .build();
```

## Error Handling

The SDK follows a **fail-open** strategy. On network or server errors, `evaluate()` returns a default response (`BotbyeDecision.ALLOW` with error details) instead of throwing:

```java
BotbyeEvaluateResponse response = botbye.evaluate(event);

if (response.getError() != null) {
    // Evaluation failed, request was allowed by default
    logger.warn(response.getError().getMessage());
}
```

## Framework Integration

### Spring Boot Filter

```java
import com.botbye.Botbye;
import com.botbye.model.BotbyeValidationEvent;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

@Component
public class BotbyeFilter extends OncePerRequestFilter {
    private final Botbye botbye;

    public BotbyeFilter(Botbye botbye) {
        this.botbye = botbye;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Map<String, String> headers = Collections.list(request.getHeaderNames()).stream()
            .collect(Collectors.toMap(h -> h, request::getHeader));

        var result = botbye.evaluate(BotbyeValidationEvent.of(
            request.getRemoteAddr(),
            request.getParameter("botbye_token"),
            headers,
            request.getMethod(),
            request.getRequestURI(),
            Collections.emptyMap()
        ));

        if (result.isBlocked()) {
            response.setStatus(403);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
```

## Testing

```bash
./gradlew build
./gradlew test
```

## License

MIT

## Support

For support, visit [botbye.com](https://botbye.com) or contact [accounts@botbye.com](mailto:accounts@botbye.com).
