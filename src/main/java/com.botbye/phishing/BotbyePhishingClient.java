package com.botbye.phishing;

import com.botbye.common.BotbyeError;
import com.botbye.common.BotbyeErrors;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Phishing-only client, keyed by the public {@link BotbyePhishingConfig#getClientKey()} — no server key
 * needed. {@link #fetchCatcher} proxies the asset via the {@code /server} route so the backend can
 * attribute it even though the browser never reaches BotBye; construction fires a best-effort init
 * handshake reporting this module via {@code Module-Name} / {@code Module-Version}.
 *
 * <p>Construct it directly and pass {@code Origin} / {@code Referer} to {@link #fetchCatcher} yourself,
 * or via {@link #withExtractor} and pass only the raw request to {@link #fetchCatcher}.
 *
 * @param <R> framework request type for the raw-request {@code fetchCatcher} methods.
 */
public class BotbyePhishingClient<R> implements Closeable {
    // A library must not install handlers or set levels on the JUL logger — that is the host's job.
    private static final Logger LOGGER = Logger.getLogger(BotbyePhishingClient.class.getName());

    private static final Map<String, String> MODULE_HEADERS = Map.of(
            "Module-Name", ModuleInfo.NAME,
            "Module-Version", ModuleInfo.VERSION);

    private static final String FORMAT_PARAM = "format";
    private static final String IMAGE_ID_PARAM = "image_id";
    private static final String EXECUTABLE_PARAM = "executable";
    private static final String MODULE_NAME_PARAM = "module_name";
    private static final String MODULE_VERSION_PARAM = "module_version";

    // Whitelist, not blacklist: the endpoint is public, so a control param the route adds later must not
    // become forwardable by default.
    private static final Set<String> FORWARDABLE_PARAMS = Set.of(MODULE_NAME_PARAM, MODULE_VERSION_PARAM);

    private static int errorStatus(String message) {
        return BotbyeErrors.TIMEOUT_ERROR.equals(message) ? 504 : 502;
    }

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

    /** Framework SDKs: bind a request-info extractor, default OkHttp transport. */
    public static <R> BotbyePhishingClient<R> withExtractor(BotbyePhishingConfig config, BotbyePhishingRequestExtractor<R> extractor) {
        return new BotbyePhishingClient<>(config, OkHttpBotbyeClient.forPhishing(), extractor, true);
    }

    /** Framework SDKs: bind a request-info extractor and your own transport. */
    public static <R> BotbyePhishingClient<R> withExtractor(BotbyePhishingConfig config, BotbyePhishingRequestExtractor<R> extractor, BotbyeHttpClient client) {
        return new BotbyePhishingClient<>(config, client, extractor, false);
    }

    public void setConf(BotbyePhishingConfig config) {
        if (config == null) {
            throw new IllegalStateException("[BotBye] phishing config is not specified");
        }

        this.config = config;
    }

    /** Releases the transport only if this client created it. */
    @Override
    public void close() {
        if (ownsClient) {
            client.close();
        }
    }

    /**
     * Fetch the catcher asset: {@link BotbyePhishingCatcher#png()} is the 1×1 pixel,
     * {@link BotbyePhishingCatcher#svg(String)} the wrapper that makes the browser fetch it.
     *
     * @param catcher which asset, and its parameters — the SVG one cannot be built without its
     *     {@code innerPngUrl}.
     * @param referer pass next to {@code origin}: an {@code <object data="…svg">} pixel sends no
     *     {@code Origin}.
     */
    public BotbyePhishingResponse fetchCatcher(BotbyePhishingCatcher catcher, String origin, String referer) {
        return fetchCatcherAsset(catcher, new BotbyePhishingRequestInfo(origin, referer));
    }

    /**
     * {@link #fetchCatcher} from a raw framework request (requires {@link #withExtractor}): the
     * extractor is the only thing that touches the request, headers and query alike.
     */
    public BotbyePhishingResponse fetchCatcher(R request, BotbyePhishingCatcher catcher) {
        return fetchCatcherAsset(catcher, extractRequestInfo(request));
    }

    private BotbyePhishingResponse fetchCatcherAsset(BotbyePhishingCatcher catcher, BotbyePhishingRequestInfo info) {
        Map<String, String> catcherQuery = forwardable(info.getQuery());
        catcherQuery.put(FORMAT_PARAM, catcher.getFormat());

        if (catcher.isSvg()) {
            catcherQuery.put(IMAGE_ID_PARAM, catcher.getInnerPngUrl().trim());
            catcherQuery.put(EXECUTABLE_PARAM, Boolean.toString(!catcher.isSkipExecution()));
        }

        return fetchAsset(info.getOrigin(), info.getReferer(), catcherQuery);
    }

    private BotbyePhishingResponse fetchAsset(String origin, String referer, Map<String, String> query) {
        // URL assembly inside the try: a surprise there is an error response, not a throw.
        try {
            String url = buildImageUrl(config, query);

            Map<String, String> headers = new HashMap<>(MODULE_HEADERS);
            String usableOrigin = usableHeaderValue(origin);
            if (usableOrigin != null) {
                headers.put("Origin", usableOrigin);
            }

            String usableReferer = usableHeaderValue(referer);
            if (usableReferer != null) {
                headers.put("Referer", usableReferer);
            }

            BotbyeHttpResponse response = client.call(new BotbyeHttpRequest(url, "GET", headers, null, null));

            return new BotbyePhishingResponse(response.getStatus(), response.getHeaders(), response.getBody());
        } catch (Exception e) {
            LOGGER.warning("[BotBye] phishing image exception occurred: " + e.getMessage());

            String message = ErrorClassifier.classify(e);

            return new BotbyePhishingResponse(
                    errorStatus(message), Collections.emptyMap(), new byte[0], new BotbyeError(message));
        }
    }

    // A null result degrades to "no headers" rather than NPE-ing in the customer's handler.
    private BotbyePhishingRequestInfo extractRequestInfo(R request) {
        BotbyePhishingRequestInfo info = requireExtractor().extract(request);

        return info == null ? new BotbyePhishingRequestInfo(null, null) : info;
    }

    private BotbyePhishingRequestExtractor<R> requireExtractor() {
        if (extractor == null) {
            throw new IllegalStateException(
                    "[BotBye] no phishing extractor configured; use BotbyePhishingClient.withExtractor(...) to fetch from a raw request");
        }

        return extractor;
    }

    // Keeps only the attribution params, so a forwarded query can never carry a control param. The value
    // is a list because a param can arrive more than once; one value per key goes on the wire.
    private static Map<String, String> forwardable(Map<String, List<String>> query) {
        if (query == null) {
            return new LinkedHashMap<>();
        }

        return query.entrySet().stream()
                .filter(param -> FORWARDABLE_PARAMS.contains(param.getKey()) && firstValue(param.getValue()) != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        param -> firstValue(param.getValue()),
                        (first, second) -> first,
                        LinkedHashMap::new));
    }

    private static String firstValue(List<String> values) {
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    // Unusable when absent, blank, or the literal "null" browsers emit for opaque origins.
    private static boolean isMissingValue(String value) {
        return value == null || value.isBlank() || value.trim().equalsIgnoreCase("null");
    }

    private static String usableHeaderValue(String value) {
        if (isMissingValue(value)) {
            return null;
        }

        String trimmed = value.trim();
        boolean needsEncoding = false;
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c >= 0x7f || (c <= 0x1f && c != '\t')) {
                needsEncoding = true;
                break;
            }
        }

        if (!needsEncoding) {
            return trimmed;
        }

        StringBuilder encoded = new StringBuilder(trimmed.length());
        for (byte b : trimmed.getBytes(StandardCharsets.UTF_8)) {
            int code = b & 0xFF;
            if (code >= 0x7F || (code <= 0x1F && code != '\t')) {
                encoded.append('%').append(String.format("%02X", code));
            } else {
                encoded.append((char) code);
            }
        }

        return encoded.toString();
    }

    private static String buildImageUrl(BotbyePhishingConfig conf, Map<String, String> query) {
        String baseUrl = conf.getEndpoint() + "/api/v1/phishing/image/" + conf.getClientKey() + "/server";

        if (query == null || query.isEmpty()) {
            return baseUrl;
        }

        StringBuilder url = new StringBuilder(baseUrl).append('?');
        boolean first = true;
        for (Map.Entry<String, String> param : query.entrySet()) {
            if (param.getKey() == null) {
                continue;
            }
            if (!first) {
                url.append('&');
            }
            // A null value is a valueless param, not a reason to fail the fetch.
            String value = param.getValue() == null ? "" : param.getValue();
            url.append(URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
            first = false;
        }

        return url.toString();
    }

    /**
     * Reports the server-side phishing integration to the backend. Best-effort: never blocks startup.
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
