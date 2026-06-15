package com.botbye.phishing;

import com.botbye.common.BotbyeError;
import com.botbye.common.ErrorClassifier;
import com.botbye.common.ModuleInfo;
import com.botbye.common.OkHttpClients;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Phishing-only client. Authenticates with the public {@link BotbyePhishingConfig#getClientKey()}
 * embedded in the URL path — it needs no server key, so it can be constructed independently of the
 * evaluate {@code com.botbye.Botbye} client.
 *
 * <p>On construction it fires a best-effort server-integration init handshake
 * ({@code POST /api/v1/phishing/init-request/v1/{clientKey}}), reporting this module via the
 * {@code Module-Name} / {@code Module-Version} headers. {@link #fetchImage} fetches the tracking
 * pixel server-side via the {@code /server} route, so the backend can attribute it to this module
 * even when the browser never reaches BotBye directly (the SDK proxies the image).
 */
public class BotbyePhishingClient {
    private static final Logger LOGGER = Logger.getLogger(BotbyePhishingClient.class.getName());

    static {
        LOGGER.setLevel(Level.WARNING);
        LOGGER.addHandler(new ConsoleHandler());
    }

    private BotbyePhishingConfig config;
    // Phishing fetchImage is an idempotent GET, so the client retries on connection failure: a stale
    // pooled keep-alive connection (closed by the server while idle) is transparently re-established
    // instead of failing with "unexpected end of stream".
    private final OkHttpClient client;

    public BotbyePhishingClient(BotbyePhishingConfig config) {
        if (config == null) {
            throw new IllegalStateException("[BotBye] phishing config is not specified");
        }

        this.config = config;
        this.client = OkHttpClients.create(
                1500,
                1500,
                250,
                Duration.ofSeconds(300),
                Duration.ofSeconds(2),
                Duration.ofSeconds(2),
                Duration.ofSeconds(2),
                Duration.ofSeconds(5),
                true
        );

        initRequest();
    }

    public void setConf(BotbyePhishingConfig config) {
        if (config == null) {
            throw new IllegalStateException("[BotBye] phishing config is not specified");
        }

        this.config = config;
    }

    public BotbyePhishingResponse fetchImage(String origin) {
        return fetchImage(origin, null);
    }

    public BotbyePhishingResponse fetchImage(String origin, String imageId) {
        BotbyePhishingConfig conf = config;

        String baseUrl = conf.getEndpoint() + "/api/v1/phishing/image/" + conf.getClientKey() + "/server";
        HttpUrl url = HttpUrl.parse(baseUrl);
        if (url == null) {
            return new BotbyePhishingResponse(0, Collections.emptyMap(), new byte[0], new BotbyeError("[BotBye] invalid phishing endpoint url"));
        }

        HttpUrl finalUrl;
        if (imageId == null || imageId.isBlank()) {
            finalUrl = url.newBuilder()
                    .addQueryParameter("format", "png")
                    .build();
        } else {
            finalUrl = url.newBuilder()
                    .addQueryParameter("image_id", imageId)
                    .addQueryParameter("format", "svg")
                    .build();
        }

        Request request = new Request.Builder()
                .url(finalUrl)
                .get()
                .addHeader("Origin", origin != null ? origin : "origin is missing")
                .addHeader("Module-Name", ModuleInfo.NAME)
                .addHeader("Module-Version", ModuleInfo.VERSION)
                .build();

        try (Response response = client.newCall(request).execute()) {
            Map<String, String> headers = new HashMap<>();
            for (String name : response.headers().names()) {
                headers.put(name, response.header(name));
            }
            ResponseBody body = response.body();
            byte[] bytes = body == null ? new byte[0] : body.bytes();

            return new BotbyePhishingResponse(response.code(), headers, bytes);
        } catch (Exception e) {
            LOGGER.warning("[BotBye] phishing image exception occurred: " + e.getMessage());
            return new BotbyePhishingResponse(0, Collections.emptyMap(), new byte[0], new BotbyeError(ErrorClassifier.classify(e)));
        }
    }

    /**
     * Reports the server-side phishing integration to the backend (the {@code SERVER_INTEGRATION_INIT}
     * get-started milestone). Best-effort: any failure is logged and swallowed, mirroring the evaluate
     * client's init handshake, so it never blocks or breaks the customer's startup.
     */
    private void initRequest() {
        try {
            String url = config.getEndpoint().replaceAll("/+$", "")
                    + "/api/v1/phishing/init-request/v1/" + config.getClientKey();

            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(new byte[0], null))
                    .header("Module-Name", ModuleInfo.NAME)
                    .header("Module-Version", ModuleInfo.VERSION)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    LOGGER.warning("[BotBye] phishing init-request returned HTTP " + response.code());
                }
            }
        } catch (Exception e) {
            LOGGER.warning("[BotBye] phishing init-request exception: " + e.getMessage());
        }
    }
}
