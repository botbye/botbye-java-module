package com.botbye;

import com.botbye.model.BotbyeConfig;
import com.botbye.model.BotbyeError;
import com.botbye.model.BotbyeEvaluateResponse;
import com.botbye.model.BotbyeEvent;
import com.botbye.model.BotbyePhishingConfig;
import com.botbye.model.BotbyePhishingResponse;
import com.botbye.model.InitRequest;
import com.botbye.model.InitResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

public class Botbye {
    private static final Logger LOGGER = Logger.getLogger(Botbye.class.getName());

    static {
        LOGGER.setLevel(Level.WARNING);
        LOGGER.addHandler(new ConsoleHandler());
    }

    private final ObjectMapper mapper = new ObjectMapper();
    private final ObjectReader reader = mapper.reader();

    private BotbyeConfig botbyeConfig;
    private BotbyePhishingConfig botbyePhishingConfig;
    private String evaluateBaseUrl;
    private final Dispatcher dispatcher = new Dispatcher();
    @SuppressWarnings("KotlinInternalInJava")
    private OkHttpClient client;
    // Phishing fetchImage is an idempotent GET; this client retries on connection
    // failure so a stale pooled keep-alive connection (closed by the server while idle)
    // is transparently re-established instead of failing with "unexpected end of stream".
    private OkHttpClient phishingClient;

    public Botbye(BotbyeConfig config) {
        setConf(config);
        initRequest();
    }

    public void setConf(BotbyeConfig config) {
        if (config == null) {
            throw new IllegalStateException("[BotBye] config is not specified");
        }

        botbyeConfig = config;
        evaluateBaseUrl = config.getBotbyeEndpoint() + "/api/v1/protect/evaluate";
        dispatcher.setMaxRequests(botbyeConfig.getMaxRequests());
        dispatcher.setMaxRequestsPerHost(botbyeConfig.getMaxRequestsPerHost());

        client = new OkHttpClient().newBuilder()
                .retryOnConnectionFailure(false)
                .dispatcher(dispatcher)
                .connectionPool(new ConnectionPool(
                        botbyeConfig.getMaxIdleConnections(),
                        botbyeConfig.getKeepAliveDuration().toMillis(),
                        TimeUnit.MILLISECONDS)
                )
                .readTimeout(botbyeConfig.getReadTimeout())
                .callTimeout(botbyeConfig.getCallTimeout())
                .connectTimeout(botbyeConfig.getConnectionTimeout())
                .writeTimeout(botbyeConfig.getWriteTimeout())
                .build();

        // Shares the dispatcher + connection pool; only flips retryOnConnectionFailure.
        phishingClient = client.newBuilder()
                .retryOnConnectionFailure(true)
                .build();
    }

    public void setPhishingConf(BotbyePhishingConfig config) {
        if (config == null) {
            throw new IllegalStateException("[BotBye] phishing config is not specified");
        }

        botbyePhishingConfig = config;
    }

    public BotbyeEvaluateResponse evaluate(BotbyeEvent event) {
        String tokenQuery = event.getUrlToken() != null ? "?" + event.getUrlToken() : "";
        String url = evaluateBaseUrl + tokenQuery;

        try {
            ObjectWriter writer = mapper.writerFor(event.getClass()).withAttribute("server_key", botbyeConfig.getServerKey());
            Request request = buildEvaluateHttpRequest(url, writer, event);
            Response response = client.newCall(request).execute();
            return handleEvaluateResponse(response);
        } catch (IOException e) {
            LOGGER.warning("[BotBye] exception occurred: " + e.getMessage());
            return new BotbyeEvaluateResponse(new BotbyeError(classifyError(e)));
        }
    }

    public CompletableFuture<BotbyeEvaluateResponse> evaluateAsync(BotbyeEvent event) {
        String tokenQuery = event.getUrlToken() != null ? "?" + event.getUrlToken() : "";
        String url = evaluateBaseUrl + tokenQuery;

        CompletableFuture<BotbyeEvaluateResponse> future = new CompletableFuture<>();
        try {
            ObjectWriter writer = mapper.writerFor(event.getClass()).withAttribute("server_key", botbyeConfig.getServerKey());
            Request request = buildEvaluateHttpRequest(url, writer, event);
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    future.complete(handleEvaluateResponse(response));
                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    LOGGER.warning("[BotBye] exception occurred: " + e.getMessage());
                    future.complete(new BotbyeEvaluateResponse(new BotbyeError(classifyError(e))));
                }
            });
        } catch (IOException e) {
            LOGGER.warning("[BotBye] exception occurred: " + e.getMessage());
            future.complete(new BotbyeEvaluateResponse(new BotbyeError(classifyError(e))));
        }

        return future;
    }

    private void initRequest() {
        try {
            String url = botbyeConfig.getBotbyeEndpoint().replaceAll("/+$", "") + "/init-request/v1";
            InitRequest initBody = new InitRequest(botbyeConfig.getServerKey());
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(mapper.writeValueAsBytes(initBody), botbyeConfig.getContentType()))
                    .header("Module-Name", BotbyeConfig.getModuleName())
                    .header("Module-Version", BotbyeConfig.getModuleVersion())
                    .header("X-Botbye-Server-Key", botbyeConfig.getServerKey())
                    .build();

            Response response = client.newCall(request).execute();
            try (ResponseBody body = response.body()) {
                if (body != null) {
                    InitResponse initResponse = reader.readValue(body.string(), InitResponse.class);
                    if (initResponse.getError() != null || !"ok".equals(initResponse.getStatus())) {
                        LOGGER.warning("[BotBye] init-request error = " + initResponse.getError() + "; status = " + initResponse.getStatus());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warning("[BotBye] exception occurred: " + e.getMessage());
        }
    }

    public BotbyePhishingResponse fetchImage(String origin) {
        return fetchImage(origin, null);
    }

    public BotbyePhishingResponse fetchImage(String origin, String imageId) {
        BotbyePhishingConfig conf = botbyePhishingConfig;
        if (conf == null) {
            return new BotbyePhishingResponse(0, Collections.emptyMap(), new byte[0], new BotbyeError("[BotBye] phishing is not configured"));
        }

        String baseUrl = conf.getEndpoint() + "/api/v1/phishing/image/" + conf.getClientKey();
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
                .build();

        try (Response response = phishingClient.newCall(request).execute()) {
            Map<String, String> headers = new HashMap<>();
            for (String name : response.headers().names()) {
                headers.put(name, response.header(name));
            }
            ResponseBody body = response.body();
            byte[] bytes = body == null ? new byte[0] : body.bytes();

            return new BotbyePhishingResponse(response.code(), headers, bytes);
        } catch (Exception e) {
            LOGGER.warning("[BotBye] phishing image exception occurred: " + e.getMessage());
            return new BotbyePhishingResponse(0, Collections.emptyMap(), new byte[0], new BotbyeError(classifyError(e)));
        }
    }

    private BotbyeEvaluateResponse handleEvaluateResponse(Response response) {
        try (ResponseBody body = response.body()) {
            if (response.code() >= 500) {
                throw new IOException("connection error: HTTP " + response.code());
            }
            if (body == null) {
                return new BotbyeEvaluateResponse();
            }
            return reader.readValue(body.string(), BotbyeEvaluateResponse.class);
        } catch (IOException e) {
            LOGGER.warning("[BotBye] exception occurred: " + e.getMessage());
            return new BotbyeEvaluateResponse(new BotbyeError(classifyError(e)));
        }
    }

    private String classifyError(Exception e) {
        if (e instanceof SocketTimeoutException) return "timeout";
        if (e instanceof ConnectException) return "connection error";
        if (e instanceof JsonProcessingException) return "invalid json response";
        if (e instanceof java.io.IOException) return "connection error";
        if (e.getMessage() != null && e.getMessage().startsWith("connection error")) return "connection error";
        return e.getMessage() != null ? e.getMessage() : "unknown error";
    }

    private Request buildEvaluateHttpRequest(String url, ObjectWriter writer, BotbyeEvent event) throws JsonProcessingException {
        return new Request.Builder()
                .url(url)
                .post(RequestBody.create(writer.writeValueAsBytes(event), botbyeConfig.getContentType()))
                .header("Module-Name", BotbyeConfig.getModuleName())
                .header("Module-Version", BotbyeConfig.getModuleVersion())
                .build();
    }

}
