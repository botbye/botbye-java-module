package com.botbye;

import com.botbye.model.BotbyeConfig;
import com.botbye.model.BotbyeError;
import com.botbye.model.BotbyePhishingConfig;
import com.botbye.model.BotbyePhishingResponse;
import com.botbye.model.BotbyeRequest;
import com.botbye.model.BotbyeResponse;
import com.botbye.model.ConnectionDetails;
import com.botbye.model.Headers;
import com.botbye.model.InitRequest;
import com.botbye.model.InitResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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

    private final ObjectReader reader = new ObjectMapper().reader();
    private final ObjectWriter writer = new ObjectMapper().registerModule(new SimpleModule().addSerializer(Headers.class, new HeadersSerializer())).writer();
    private BotbyeConfig botbyeConfig;
    private BotbyePhishingConfig botbyePhishingConfig;
    private final Dispatcher dispatcher = new Dispatcher();
    @SuppressWarnings("KotlinInternalInJava")
    private OkHttpClient client;

    public Botbye(BotbyeConfig config) {
        setConf(config);
        initRequest();
    }

    public void setConf(BotbyeConfig config) {
        if (config == null) {
            throw new IllegalStateException("[BotBye] config is not specified");
        }

        botbyeConfig = config;
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
    }

    public void setPhishingConf(BotbyePhishingConfig config) {
        if (config == null) {
            throw new IllegalStateException("[BotBye] phishing config is not specified");
        }

        botbyePhishingConfig = config;
    }

    public BotbyeResponse validateRequest(String token, ConnectionDetails connectionDetails, Headers headers) {
        return validateRequest(token, connectionDetails, headers, Collections.emptyMap());
    }

    public BotbyeResponse validateRequest(String token, ConnectionDetails connectionDetails, Headers headers, Map<String, String> customFields) {
        BotbyeRequest body = createBotbyeRequestBody(headers, connectionDetails, customFields);

        try {
            Request request = createRequest(token, body);
            Response response = client.newCall(request).execute();
            return handleResponse(response);
        } catch (IOException e) {
            LOGGER.warning("[BotBye] exception occurred: " + e.getMessage());
            return new BotbyeResponse(new BotbyeError(e.getMessage()));
        }
    }

    public CompletableFuture<BotbyeResponse> validateRequestAsync(String token, ConnectionDetails connectionDetails, Headers headers) {
        return validateRequestAsync(token, connectionDetails, headers, Collections.emptyMap());
    }

    public CompletableFuture<BotbyeResponse> validateRequestAsync(String token, ConnectionDetails connectionDetails, Headers headers, Map<String, String> customFields) {
        BotbyeRequest body = createBotbyeRequestBody(headers, connectionDetails, customFields);

        CompletableFuture<BotbyeResponse> future = new CompletableFuture<>();
        try {
            Request request = createRequest(token, body);
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    future.complete(handleResponse(response));
                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    LOGGER.warning("[BotBye] exception occurred: " + e.getMessage());
                    future.complete(new BotbyeResponse(new BotbyeError(e.getMessage())));
                }
            });
            return future;
        } catch (IOException e) {
            LOGGER.warning("[BotBye] exception occurred: " + e.getMessage());
            future.complete(new BotbyeResponse(new BotbyeError(e.getMessage())));
            return future;
        }
    }

    private void initRequest() {
        try {
            String url = botbyeConfig.getBotbyeEndpoint().replaceAll("/+$", "") + "/init-request/v1";
            InitRequest initBody = new InitRequest(botbyeConfig.getServerKey());
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(writer.writeValueAsString(initBody), botbyeConfig.getContentType()))
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

        String baseUrl = conf.getEndpoint() + "/api/v1/phishing/" + conf.getAccountId() + "/projects/" + conf.getProjectId() + "/image";
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

        Request request = createPhishingRequest(finalUrl, origin, conf.getApiKey());

        try (Response response = client.newCall(request).execute()) {
            return getBotbyePhishingResponse(response);
        } catch (Exception e) {
            LOGGER.warning("[BotBye] phishing image exception occurred: " + e.getMessage());
            return new BotbyePhishingResponse(0, Collections.emptyMap(), new byte[0], new BotbyeError(e.getMessage() != null ? e.getMessage() : "[BotBye] failed to fetch phishing image"));
        }
    }

    @NotNull
    private BotbyePhishingResponse getBotbyePhishingResponse(Response response) throws IOException {
        Map<String, String> headers = new HashMap<>();
        for (String name : response.headers().names()) {
            headers.put(name, response.header(name));
        }

        ResponseBody body = response.body();
        byte[] bytes = body == null ? new byte[0] : body.bytes();

        return new BotbyePhishingResponse(response.code(), headers, bytes);
    }


    private BotbyeResponse handleResponse(Response response) {
        try (ResponseBody body = response.body()) {
            if (body == null) {
                return new BotbyeResponse();
            }
            return reader.readValue(body.string(), BotbyeResponse.class);
        } catch (IOException e) {
            LOGGER.warning("[BotBye] exception occurred: " + e.getMessage());
            return new BotbyeResponse(new BotbyeError(e.getMessage()));
        }
    }

    private BotbyeRequest createBotbyeRequestBody(Headers headers, ConnectionDetails connectionDetails, Map<String, String> customFields) {
        return new BotbyeRequest(botbyeConfig.getServerKey(), headers, connectionDetails, customFields);
    }

    private Request createPhishingRequest(HttpUrl url, String origin, String apiKey) {
        return new Request.Builder()
                .url(url)
                .get()
                .addHeader("X-Api-Key", apiKey)
                .addHeader("Origin", origin != null ? origin : "origin is missing")
                .build();
    }

    private Request createRequest(String token, BotbyeRequest body) throws JsonProcessingException {
        String url = botbyeConfig.getBotbyeEndpoint() + "/validate-request/v2?" + Optional.ofNullable(token).orElse("");

        return new Request.Builder()
                .url(url)
                .post(RequestBody.create(writer.writeValueAsString(body), botbyeConfig.getContentType()))
                .header("Module-Name", BotbyeConfig.getModuleName())
                .header("Module-Version", BotbyeConfig.getModuleVersion())
                .build();
    }

}
