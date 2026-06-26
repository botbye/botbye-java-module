# BotBye Java Module

Java SDK for the [BotBye](https://botbye.com) Unified Protection Platform — unifying fraud prevention and real-time event monitoring in one platform.

BotBye goes beyond fixed bot/ATO checks. Risk dimensions and metrics are fully dynamic — you define what to measure and what rules to apply per project. This means the same platform covers bot detection, account takeover, multi-accounting, payment fraud, promotion abuse, or any custom fraud scenario specific to your business.

## Requirements

- Java 11 or higher
- Gradle or Maven

## Installation

### Gradle (Kotlin DSL)

```kotlin
implementation("com.botbye:java-module:3.0.1")
```

### Gradle (Groovy DSL)

```groovy
implementation 'com.botbye:java-module:3.0.1'
```

### Maven

```xml
<dependency>
    <groupId>com.botbye</groupId>
    <artifactId>java-module</artifactId>
    <version>3.0.1</version>
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
import com.botbye.protection.Botbye;
import com.botbye.protection.BotbyeConfig;

BotbyeConfig config = new BotbyeConfig.Builder()
    .serverKey("your-server-key") // from https://app.botbye.com
    .build();

Botbye botbye = new Botbye(config);
```

### 2. Bot Validation (Level 1)

Validate device tokens where user identity is not yet available — at the proxy layer or in a middleware before authentication.

Headers are passed as a `com.botbye.common.http.Headers` wrapping your framework's **multi-value** headers (`Map<String, List<String>>`). The SDK owns the normalization (lowercased keys, comma-joined values) at serialization time, so you never flatten them yourself:

```java
import com.botbye.common.http.Headers;

// Build a Headers from your servlet request once, reuse everywhere.
Headers headersOf(HttpServletRequest request) {
    Map<String, List<String>> map = new HashMap<>();
    for (String name : Collections.list(request.getHeaderNames())) {
        map.put(name, Collections.list(request.getHeaders(name)));
    }
    return new Headers(map);
}
```

```java
import com.botbye.protection.model.BotbyeValidationEvent;

Headers headers = headersOf(request);

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
import com.botbye.protection.model.BotbyeRiskScoringEvent;
import com.botbye.protection.model.BotbyeUserInfo;
import com.botbye.protection.model.BotbyeEventStatus;
import com.botbye.protection.model.BotbyeDecision;

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
import com.botbye.protection.model.BotbyeFullEvent;

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

The raw-request convenience methods (see [Request Extractors](#request-extractors-framework-integration))
have async variants too — `evaluateValidationAsync`, `evaluateRiskScoringAsync`, and
`evaluateFullAsync` — with the same signatures as their synchronous counterparts:

```java
CompletableFuture<BotbyeEvaluateResponse> future = botbye.evaluateValidationAsync(request);
```

### 5. Phishing Image Tracking

The phishing tracking pixel is embedded on a protected site; when a phishing clone copies the
markup, the pixel is requested with the clone's `Origin`, which lets BotBye record a phishing
candidate.

Phishing lives in its own dedicated `BotbyePhishingClient` — **separate from the evaluate `Botbye`
client**. The project is identified by a public, browser-safe `clientKey` in the URL path, so the
client needs **no server key**; you can construct it standalone. On construction it fires a
best-effort server-integration init handshake (`POST /api/v1/phishing/init-request/v1/{clientKey}`)
reporting this module, and `fetchImage` proxies the pixel via the server `/server` route so the
backend can attribute it to this module even when the browser never reaches BotBye directly.

```java
import com.botbye.phishing.BotbyePhishingClient;
import com.botbye.phishing.BotbyePhishingConfig;
import com.botbye.phishing.BotbyePhishingResponse;

BotbyePhishingClient phishing = new BotbyePhishingClient(
    new BotbyePhishingConfig.Builder()
        .endpoint("https://verify.botbye.com") // default
        .clientKey("<public-client-key>")
        .build()
);

// Proxy the browser's pixel request: forward its original query verbatim (it carries
// format / image_id and the JS tag's module_name / module_version).
Map<String, String> query = request.getParameterMap().entrySet().stream()
    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()[0]));

BotbyePhishingResponse res = phishing.fetchImage(request.getHeader("Origin"), query);

res.getStatus();   // 200
res.getHeaders();  // {Content-Type=image/png, ...}
res.getBody();     // byte[] — raw image bytes to relay back to the browser
res.getError();    // BotbyeError — non-null on transport failure
```

`fetchImage` returns `BotbyePhishingResponse`:

| Field | Type | Description |
|---|---|---|
| `status` | `int` | Upstream HTTP status (`0` on transport failure) |
| `headers` | `Map<String, String>` | Response headers (e.g. `Content-Type`) |
| `body` | `byte[]` | Raw image bytes (PNG or SVG, per the forwarded `format` query param) |
| `error` | `BotbyeError` | Normalized transport error: `timeout`, `connection error`, or `invalid json response` |

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

## Request Extractors (framework integration)

Instead of building events field-by-field at every call site, describe **once** how to turn your
framework's request object into a `BotbyeRequestInfo`, then pass only the raw request to the
`evaluate*` methods. Build the client with `Botbye.withExtractor(...)` — the type parameter is your
framework request type:

```java
import com.botbye.protection.Botbye;
import com.botbye.protection.model.BotbyeRequestInfo;
import com.botbye.common.http.Headers;
import jakarta.servlet.http.HttpServletRequest;

Botbye<HttpServletRequest> botbye = Botbye.withExtractor(config, request -> {
    Map<String, List<String>> map = new HashMap<>();
    for (String name : Collections.list(request.getHeaderNames())) {
        map.put(name, Collections.list(request.getHeaders(name)));
    }

    return new BotbyeRequestInfo(
        request.getRemoteAddr(),
        request.getParameter("botbye_token"),
        new Headers(map),
        request.getMethod(),
        request.getRequestURI()
    );
});
```

Now the call sites only pass the raw request (plus user/event for Level 2):

```java
import com.botbye.protection.model.BotbyeEventStatus;
import com.botbye.protection.model.BotbyeUserInfo;

// Level 1 — bot validation
BotbyeEvaluateResponse l1 = botbye.evaluateValidation(request);

// Level 2 — risk scoring & event logging
BotbyeEvaluateResponse l2 = botbye.evaluateRiskScoring(
    request,
    new BotbyeUserInfo(userId),
    "LOGIN",
    BotbyeEventStatus.SUCCESSFUL,
    l1.getBotbyeResult(),       // botbyeResult — links to the Level 1 result
    Collections.emptyMap()
);

// Level 1+2 combined (no separate proxy)
BotbyeEvaluateResponse full = botbye.evaluateFull(request, new BotbyeUserInfo(userId), "LOGIN", BotbyeEventStatus.FAILED);
```

For `evaluateValidation` and `evaluateFull`, an explicit `token` argument overrides the one returned
by the extractor. `evaluateRiskScoring` takes no token — Level 2 links to Level 1 via `botbyeResult`;
a token together with user/event context is a combined call, so use `evaluateFull` instead.

Each of these has a non-blocking `*Async` variant with an identical signature that returns a
`CompletableFuture<BotbyeEvaluateResponse>`:

```java
// Level 1 — bot validation, non-blocking
botbye.evaluateValidationAsync(request)
    .thenAccept(response -> {
        if (response.isBlocked()) { /* handle blocked request */ }
    });

// Level 2 and combined have async variants too:
botbye.evaluateRiskScoringAsync(request, new BotbyeUserInfo(userId), "LOGIN", BotbyeEventStatus.SUCCESSFUL);
botbye.evaluateFullAsync(request, new BotbyeUserInfo(userId), "LOGIN", BotbyeEventStatus.FAILED);
```

### Spring Boot Filter

```java
@Component
public class BotbyeFilter extends OncePerRequestFilter {
    private final Botbye<HttpServletRequest> botbye;

    public BotbyeFilter(Botbye<HttpServletRequest> botbye) {
        this.botbye = botbye;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (botbye.evaluateValidation(request).isBlocked()) {
            response.setStatus(403);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
```

> The explicit-event API is always available too: `new Botbye(config)` gives a client on which you
> call `evaluate(BotbyeValidationEvent.of(...))` etc. with no extractor.

## Custom HTTP Transport

The SDK depends only on the `BotbyeHttpClient` interface; OkHttp is the default
(`OkHttpBotbyeClient`). To run on a different HTTP stack, implement the interface and pass it to the
client constructor / factory:

```java
import com.botbye.common.http.BotbyeHttpClient;
import com.botbye.common.http.BotbyeHttpRequest;
import com.botbye.common.http.BotbyeHttpResponse;

class MyHttpClient implements BotbyeHttpClient {
    public String type() { return "my-client"; }
    public BotbyeHttpResponse call(BotbyeHttpRequest request) throws IOException { /* ... */ }
    public CompletableFuture<BotbyeHttpResponse> callAsync(BotbyeHttpRequest request) { /* ... */ }
}

Botbye botbye = new Botbye(config, new MyHttpClient());
```

## Lifecycle

Construct the client **once** and reuse it for the lifetime of your application — it owns a connection
pool and a dispatcher thread pool. Both `Botbye` and `BotbyePhishingClient` implement `Closeable`:

```java
botbye.close(); // shuts down the default OkHttp transport (dispatcher + connection pool)
```

`close()` only shuts down the transport the SDK created for you. If you passed your own
`BotbyeHttpClient`, the SDK never closes it — you own its lifecycle.

## Helpers

| Helper | Description |
|---|---|
| `FallbackEvaluationResult.create(message)` | Build a fail-open `BotbyeEvaluateResponse` (`ALLOW` + `error`) for your own short-circuit paths. |
| `BotbyeErrors` | Normalized error message constants: `SDK_ERROR`, `UNKNOWN_ERROR`, `TIMEOUT_ERROR`, `CONNECTION_ERROR`, `JSON_ERROR`. |

## Testing

```bash
./gradlew build
./gradlew test
```

## License

MIT

## Support

For support, visit [botbye.com](https://botbye.com) or contact [accounts@botbye.com](mailto:accounts@botbye.com).
