package com.botbye.protection;

import com.botbye.common.BotbyeErrors;
import com.botbye.common.ErrorClassifier;
import com.botbye.common.ModuleInfo;
import com.botbye.common.http.BotbyeHttpClient;
import com.botbye.common.http.BotbyeHttpRequest;
import com.botbye.common.http.BotbyeHttpResponse;
import com.botbye.common.http.Headers;
import com.botbye.common.http.HeadersSerializer;
import com.botbye.common.http.OkHttpBotbyeClient;
import com.botbye.protection.model.BotbyeEvaluateResponse;
import com.botbye.protection.model.BotbyeEvent;
import com.botbye.protection.model.BotbyeEventInfo;
import com.botbye.protection.model.BotbyeEventStatus;
import com.botbye.protection.model.BotbyeFullEvent;
import com.botbye.protection.model.BotbyeRequestInfo;
import com.botbye.protection.model.BotbyeRiskScoringEvent;
import com.botbye.protection.model.BotbyeUserInfo;
import com.botbye.protection.model.BotbyeValidationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.Closeable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Evaluate client (Level 1/2 bot &amp; risk scoring). Requires a server key and runs an init
 * handshake on construction. Phishing image tracking lives in
 * {@link com.botbye.phishing.BotbyePhishingClient}.
 *
 * <p>Two construction modes:
 * <ul>
 *   <li>{@code new Botbye(config)} — explicit-event client; build events yourself and call
 *       {@link #evaluate(BotbyeEvent)}.</li>
 *   <li>{@link #withExtractor} — binds a {@link BotbyeRequestExtractor} of framework request type
 *       {@code R} so callers pass only their raw request to {@link #evaluateValidation},
 *       {@link #evaluateRiskScoring}, {@link #evaluateFull}.</li>
 * </ul>
 *
 * @param <R> framework request type for the raw-request {@code evaluate*} methods; irrelevant when
 *            no extractor is configured.
 */
public class Botbye<R> implements BotbyeEvaluator, Closeable {
    private static final Logger LOGGER = Logger.getLogger(Botbye.class.getName());

    static {
        LOGGER.setLevel(Level.WARNING);
        LOGGER.addHandler(new ConsoleHandler());
    }

    private static final Map<String, String> MODULE_HEADERS = Map.of(
            "Module-Name", ModuleInfo.NAME,
            "Module-Version", ModuleInfo.VERSION);

    private final ObjectMapper mapper = createMapper();
    private final BotbyeHttpClient client;
    private final BotbyeRequestExtractor<R> extractor;
    private final boolean ownsClient;

    // volatile so a concurrent setConf() publishes the new endpoint/key, and evaluate() reads a single
    // consistent snapshot into a local (no torn endpoint-from-old / key-from-new).
    private volatile BotbyeConfig botbyeConfig;

    private Botbye(BotbyeConfig config, BotbyeHttpClient client, BotbyeRequestExtractor<R> extractor, boolean ownsClient) {
        if (client == null) {
            throw new IllegalStateException("[BotBye] http client is not specified");
        }

        this.client = client;
        this.extractor = extractor;
        this.ownsClient = ownsClient;

        applyConfig(config);
        initRequest();
    }

    /** Explicit-event client with the default OkHttp transport. */
    public Botbye(BotbyeConfig config) {
        this(config, defaultClient(config), null, true);
    }

    /** Explicit-event client with a custom (caller-owned) transport. */
    public Botbye(BotbyeConfig config, BotbyeHttpClient client) {
        this(config, client, null, false);
    }

    /** Factory for framework SDKs: bind a request extractor and use the default OkHttp transport. */
    public static <R> Botbye<R> withExtractor(BotbyeConfig config, BotbyeRequestExtractor<R> extractor) {
        return new Botbye<>(config, defaultClient(config), extractor, true);
    }

    /** Factory for framework SDKs: bind a request extractor and a custom (caller-owned) transport. */
    public static <R> Botbye<R> withExtractor(BotbyeConfig config, BotbyeRequestExtractor<R> extractor, BotbyeHttpClient client) {
        return new Botbye<>(config, client, extractor, false);
    }

    private static ObjectMapper createMapper() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Headers.class, new HeadersSerializer());

        return new ObjectMapper().registerModule(module);
    }

    private static BotbyeHttpClient defaultClient(BotbyeConfig config) {
        if (config == null) {
            throw new IllegalStateException("[BotBye] config is not specified");
        }

        return OkHttpBotbyeClient.forEvaluate(
                config.getMaxRequests(),
                config.getMaxRequestsPerHost(),
                config.getMaxIdleConnections(),
                config.getKeepAliveDuration(),
                config.getReadTimeout(),
                config.getWriteTimeout(),
                config.getConnectionTimeout(),
                config.getCallTimeout()
        );
    }

    @Override
    public void setConf(BotbyeConfig config) {
        applyConfig(config);
    }

    /** Send a fully-built event for risk evaluation. Fails open: returns ALLOW + error on failure. */
    @Override
    public BotbyeEvaluateResponse evaluate(BotbyeEvent event) {
        try {
            BotbyeHttpRequest request = buildEvaluateRequest(event);
            return handleEvaluateResponse(client.call(request));
        } catch (Exception e) {
            LOGGER.warning("[BotBye] exception occurred: " + e.getMessage());
            return FallbackEvaluationResult.create(ErrorClassifier.classify(e));
        }
    }

    /** Asynchronous variant of {@link #evaluate(BotbyeEvent)}. */
    @Override
    public CompletableFuture<BotbyeEvaluateResponse> evaluateAsync(BotbyeEvent event) {
        try {
            BotbyeHttpRequest request = buildEvaluateRequest(event);
            return client.callAsync(request).handle((response, error) -> {
                if (error != null) {
                    LOGGER.warning("[BotBye] exception occurred: " + error.getMessage());
                    return FallbackEvaluationResult.create(classifyThrowable(error));
                }
                return handleEvaluateResponse(response);
            });
        } catch (Exception e) {
            LOGGER.warning("[BotBye] exception occurred: " + e.getMessage());
            return CompletableFuture.completedFuture(FallbackEvaluationResult.create(ErrorClassifier.classify(e)));
        }
    }

    /** Level 1 bot validation from a raw framework request (requires {@link #withExtractor}). */
    public BotbyeEvaluateResponse evaluateValidation(R request) {
        return evaluateValidation(request, null, null);
    }

    /** Level 1 bot validation from a raw framework request (requires {@link #withExtractor}). */
    public BotbyeEvaluateResponse evaluateValidation(R request, String token, Map<String, String> customFields) {
        BotbyeRequestInfo info = withToken(requireExtractor().extract(request), token);

        return evaluate(new BotbyeValidationEvent(info, orEmpty(customFields)));
    }

    /** Level 2 risk evaluation from a raw framework request (requires {@link #withExtractor}). */
    public BotbyeEvaluateResponse evaluateRiskScoring(R request, BotbyeUserInfo user, String eventType, BotbyeEventStatus eventStatus) {
        return evaluateRiskScoring(request, user, eventType, eventStatus, null, null, null);
    }

    /** Level 2 risk evaluation from a raw framework request (requires {@link #withExtractor}). */
    public BotbyeEvaluateResponse evaluateRiskScoring(
            R request,
            BotbyeUserInfo user,
            String eventType,
            BotbyeEventStatus eventStatus,
            String token,
            String botbyeResult,
            Map<String, String> customFields
    ) {
        BotbyeRequestInfo info = withToken(requireExtractor().extract(request), token);
        String result = botbyeResult != null && !botbyeResult.isBlank() ? botbyeResult : null;

        return evaluate(new BotbyeRiskScoringEvent(info, new BotbyeEventInfo(eventType, eventStatus), user, result, orEmpty(customFields)));
    }

    /** Combined Level 1+2 evaluation from a raw framework request (requires {@link #withExtractor}). */
    public BotbyeEvaluateResponse evaluateFull(R request, BotbyeUserInfo user, String eventType, BotbyeEventStatus eventStatus) {
        return evaluateFull(request, user, eventType, eventStatus, null, null);
    }

    /** Combined Level 1+2 evaluation from a raw framework request (requires {@link #withExtractor}). */
    public BotbyeEvaluateResponse evaluateFull(
            R request,
            BotbyeUserInfo user,
            String eventType,
            BotbyeEventStatus eventStatus,
            String token,
            Map<String, String> customFields
    ) {
        BotbyeRequestInfo info = withToken(requireExtractor().extract(request), token);

        return evaluate(new BotbyeFullEvent(info, new BotbyeEventInfo(eventType, eventStatus), user, orEmpty(customFields)));
    }

    /** Releases the underlying transport only if this client created it (a passed-in client is left alone). */
    @Override
    public void close() {
        if (ownsClient) {
            client.close();
        }
    }

    private void applyConfig(BotbyeConfig config) {
        if (config == null) {
            throw new IllegalStateException("[BotBye] config is not specified");
        }

        this.botbyeConfig = config;
    }

    private BotbyeRequestExtractor<R> requireExtractor() {
        if (extractor == null) {
            throw new IllegalStateException(
                    "[BotBye] no requestInfoExtractor configured; use Botbye.withExtractor(...) for raw-request evaluate*");
        }
        return extractor;
    }

    private BotbyeHttpRequest buildEvaluateRequest(BotbyeEvent event) throws com.fasterxml.jackson.core.JsonProcessingException {
        BotbyeConfig config = botbyeConfig;
        String url = config.getBotbyeEndpoint() + "/api/v1/protect/evaluate" + (event.getUrlToken() != null ? "?" + event.getUrlToken() : "");
        ObjectWriter writer = mapper.writerFor(event.getClass()).withAttribute("server_key", config.getServerKey());

        return new BotbyeHttpRequest(url, "POST", MODULE_HEADERS, writer.writeValueAsBytes(event), config.getContentType());
    }

    private BotbyeEvaluateResponse handleEvaluateResponse(BotbyeHttpResponse response) {
        if (response.getStatus() >= 500) {
            return FallbackEvaluationResult.create(BotbyeErrors.CONNECTION_ERROR);
        }
        if (response.getBody().length == 0) {
            return new BotbyeEvaluateResponse();
        }

        try {
            return mapper.readValue(response.getBody(), BotbyeEvaluateResponse.class);
        } catch (Exception e) {
            LOGGER.warning("[BotBye] exception occurred: " + e.getMessage());
            return FallbackEvaluationResult.create(ErrorClassifier.classify(e));
        }
    }

    private void initRequest() {
        try {
            BotbyeConfig config = botbyeConfig;
            String url = config.getBotbyeEndpoint().replaceAll("/+$", "") + "/init-request/v1";

            BotbyeHttpRequest request = new BotbyeHttpRequest(
                    url, "POST", MODULE_HEADERS,
                    mapper.writeValueAsBytes(new InitRequest(config.getServerKey())),
                    config.getContentType());

            BotbyeHttpResponse response = client.call(request);
            if (response.getBody().length > 0) {
                InitResponse initResponse = mapper.readValue(response.getBody(), InitResponse.class);
                if (initResponse.getError() != null || !"ok".equals(initResponse.getStatus())) {
                    LOGGER.warning("[BotBye] init-request error = " + initResponse.getError() + "; status = " + initResponse.getStatus());
                }
            }
        } catch (Exception e) {
            LOGGER.warning("[BotBye] exception occurred: " + e.getMessage());
        }
    }

    private static BotbyeRequestInfo withToken(BotbyeRequestInfo base, String token) {
        if (token == null) {
            return base;
        }

        return new BotbyeRequestInfo(base.getIp(), token, base.getHeaders(), base.getRequestMethod(), base.getRequestUri());
    }

    private static Map<String, String> orEmpty(Map<String, String> customFields) {
        return customFields != null ? customFields : Collections.emptyMap();
    }

    private static String classifyThrowable(Throwable t) {
        Throwable cause = t instanceof CompletionException && t.getCause() != null ? t.getCause() : t;

        return cause instanceof Exception ? ErrorClassifier.classify((Exception) cause) : BotbyeErrors.UNKNOWN_ERROR;
    }
}
