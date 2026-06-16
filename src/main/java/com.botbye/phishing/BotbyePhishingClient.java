package com.botbye.phishing;

import com.botbye.common.BotbyeError;
import com.botbye.common.ErrorClassifier;
import com.botbye.common.ModuleInfo;
import com.botbye.common.http.BotbyeHttpClient;
import com.botbye.common.http.BotbyeHttpRequest;
import com.botbye.common.http.BotbyeHttpResponse;
import com.botbye.common.http.OkHttpBotbyeClient;
import java.io.Closeable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Phishing-only client. Authenticates with the public {@link BotbyePhishingConfig#getClientKey()}
 * embedded in the URL path — it needs no server key, so it can be constructed independently of the
 * evaluate {@code com.botbye.protection.Botbye} client.
 *
 * <p>On construction it fires a best-effort server-integration init handshake
 * ({@code POST /api/v1/phishing/init-request/v1/{clientKey}}), reporting this module via the
 * {@code Module-Name} / {@code Module-Version} headers. {@link #fetchImage} fetches the tracking
 * pixel server-side via the {@code /server} route, so the backend can attribute it to this module
 * even when the browser never reaches BotBye directly (the SDK proxies the image).
 *
 * <p>Two construction modes:
 * <ul>
 *   <li>{@code new BotbyePhishingClient(config)} — pass the {@code Origin} header to
 *       {@link #fetchImage(String)} yourself.</li>
 *   <li>{@link #withExtractor} — binds a {@link BotbyePhishingRequestExtractor} of framework request
 *       type {@code R} so callers pass only their raw request to {@link #fetchImage(Object)}.</li>
 * </ul>
 *
 * @param <R> framework request type for the raw-request {@code fetchImage} methods.
 */
public class BotbyePhishingClient<R> implements Closeable {
    private static final Logger LOGGER = Logger.getLogger(BotbyePhishingClient.class.getName());

    static {
        LOGGER.setLevel(Level.WARNING);
        LOGGER.addHandler(new ConsoleHandler());
    }

    private static final Map<String, String> MODULE_HEADERS = Map.of(
            "Module-Name", ModuleInfo.NAME,
            "Module-Version", ModuleInfo.VERSION);

    // volatile so a concurrent setConf() publishes the new config to other threads.
    private volatile BotbyePhishingConfig config;
    private final BotbyeHttpClient client;
    private final BotbyePhishingRequestExtractor<R> extractor;
    private final boolean ownsClient;

    private BotbyePhishingClient(BotbyePhishingConfig config, BotbyeHttpClient client, BotbyePhishingRequestExtractor<R> extractor, boolean ownsClient) {
        if (config == null) {
            throw new IllegalStateException("[BotBye] phishing config is not specified");
        }
        if (client == null) {
            throw new IllegalStateException("[BotBye] http client is not specified");
        }

        this.config = config;
        this.client = client;
        this.extractor = extractor;
        this.ownsClient = ownsClient;

        initRequest();
    }

    public BotbyePhishingClient(BotbyePhishingConfig config) {
        this(config, OkHttpBotbyeClient.forPhishing(), null, true);
    }

    public BotbyePhishingClient(BotbyePhishingConfig config, BotbyeHttpClient client) {
        this(config, client, null, false);
    }

    /** Factory for framework SDKs: bind an Origin extractor and use the default OkHttp transport. */
    public static <R> BotbyePhishingClient<R> withExtractor(BotbyePhishingConfig config, BotbyePhishingRequestExtractor<R> extractor) {
        return new BotbyePhishingClient<>(config, OkHttpBotbyeClient.forPhishing(), extractor, true);
    }

    /** Factory for framework SDKs: bind an Origin extractor and a custom (caller-owned) transport. */
    public static <R> BotbyePhishingClient<R> withExtractor(BotbyePhishingConfig config, BotbyePhishingRequestExtractor<R> extractor, BotbyeHttpClient client) {
        return new BotbyePhishingClient<>(config, client, extractor, false);
    }

    public void setConf(BotbyePhishingConfig config) {
        if (config == null) {
            throw new IllegalStateException("[BotBye] phishing config is not specified");
        }

        this.config = config;
    }

    /** Releases the underlying transport only if this client created it (a passed-in client is left alone). */
    @Override
    public void close() {
        if (ownsClient) {
            client.close();
        }
    }

    /** Fetch the tracking pixel using an explicit {@code Origin} header value. */
    public BotbyePhishingResponse fetchImage(String origin) {
        return fetchImage(origin, Collections.emptyMap());
    }

    /**
     * Fetch the tracking pixel using an explicit {@code Origin} header value.
     *
     * <p>{@code query} is forwarded verbatim to the {@code /server} route — pass the browser's
     * original pixel query (which carries {@code format}, {@code image_id}, and the JS tag's
     * {@code module_name} / {@code module_version}).
     */
    public BotbyePhishingResponse fetchImage(String origin, Map<String, String> query) {
        String url = buildImageUrl(config, query);

        Map<String, String> headers = new HashMap<>(MODULE_HEADERS);
        headers.put("Origin", origin != null ? origin : "origin is missing");

        try {
            BotbyeHttpResponse response = client.call(new BotbyeHttpRequest(url, "GET", headers, null, null));

            return new BotbyePhishingResponse(response.getStatus(), response.getHeaders(), response.getBody());
        } catch (Exception e) {
            LOGGER.warning("[BotBye] phishing image exception occurred: " + e.getMessage());
            return new BotbyePhishingResponse(0, Collections.emptyMap(), new byte[0], new BotbyeError(ErrorClassifier.classify(e)));
        }
    }

    /** Fetch the tracking pixel from a raw framework request (requires {@link #withExtractor}). */
    public BotbyePhishingResponse fetchImage(R request) {
        return fetchImage(request, Collections.emptyMap());
    }

    /** Fetch the tracking pixel from a raw framework request (requires {@link #withExtractor}). */
    public BotbyePhishingResponse fetchImage(R request, Map<String, String> query) {
        if (extractor == null) {
            throw new IllegalStateException(
                    "[BotBye] no phishing extractor configured; use BotbyePhishingClient.withExtractor(...) to fetch from a raw request");
        }

        return fetchImage(extractor.extractOrigin(request), query);
    }

    private static String buildImageUrl(BotbyePhishingConfig conf, Map<String, String> query) {
        String baseUrl = conf.getEndpoint() + "/api/v1/phishing/image/" + conf.getClientKey() + "/server";

        if (query == null || query.isEmpty()) {
            return baseUrl;
        }

        StringBuilder url = new StringBuilder(baseUrl).append('?');
        boolean first = true;
        for (Map.Entry<String, String> param : query.entrySet()) {
            if (!first) {
                url.append('&');
            }
            url.append(URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8));
            first = false;
        }

        return url.toString();
    }

    /**
     * Reports the server-side phishing integration to the backend (the {@code SERVER_INTEGRATION_INIT}
     * get-started milestone). Best-effort: any failure is logged and swallowed, mirroring the evaluate
     * client's init handshake, so it never blocks or breaks the customer's startup.
     */
    private void initRequest() {
        try {
            BotbyePhishingConfig conf = config;
            String url = conf.getEndpoint().replaceAll("/+$", "")
                    + "/api/v1/phishing/init-request/v1/" + conf.getClientKey();

            BotbyeHttpResponse response = client.call(new BotbyeHttpRequest(url, "POST", MODULE_HEADERS, new byte[0], null));
            if (response.getStatus() < 200 || response.getStatus() >= 300) {
                LOGGER.warning("[BotBye] phishing init-request returned HTTP " + response.getStatus());
            }
        } catch (Exception e) {
            LOGGER.warning("[BotBye] phishing init-request exception: " + e.getMessage());
        }
    }
}
