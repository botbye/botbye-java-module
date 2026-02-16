package com.botbye;

import com.botbye.model.BotbyeConfig;
import com.botbye.model.BotbyeError;
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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
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
    private BotbyeConfig botbyeConfig = new BotbyeConfig();
    private final Dispatcher dispatcher = new Dispatcher();
    @SuppressWarnings("KotlinInternalInJava")
    private final OkHttpClient client = new OkHttpClient().newBuilder()
            .retryOnConnectionFailure(false)
            .dispatcher(dispatcher)
            .connectionPool(new ConnectionPool(
                    botbyeConfig.getMaxIdleConnections(),
                    botbyeConfig.getKeepAliveDuration(),
                    botbyeConfig.getKeepAliveDurationTimeUnit())
            )
            .readTimeout(botbyeConfig.getReadTimeout())
            .callTimeout(botbyeConfig.getCallTimeout())
            .connectTimeout(botbyeConfig.getConnectionTimeout())
            .writeTimeout(botbyeConfig.getWriteTimeout())
            .build();

    public Botbye() {
        initRequest();
    }

    public Botbye(BotbyeConfig config) {
        botbyeConfig = config;
        dispatcher.setMaxRequests(botbyeConfig.getMaxRequests());
        dispatcher.setMaxRequestsPerHost(botbyeConfig.getMaxRequestsPerHost());
        initRequest();
    }

    public void setConf(BotbyeConfig config) {
        botbyeConfig = config;
        dispatcher.setMaxRequests(botbyeConfig.getMaxRequests());
        dispatcher.setMaxRequestsPerHost(botbyeConfig.getMaxRequestsPerHost());
    }

    public BotbyeResponse validateRequest(String token, ConnectionDetails connectionDetails, Headers headers) {
        return validateRequest(token, connectionDetails, headers, Collections.emptyMap());
    }

    public BotbyeResponse validateRequest(String token, ConnectionDetails connectionDetails, Headers headers, Map<String, String> customFields) {
        validateServerKey();

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
        validateServerKey();
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

    private void validateServerKey() {
        if (botbyeConfig.getServerKey().isBlank()) {
            throw new IllegalStateException("[BotBye] server key is not specified");
        }
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

    private Request createRequest(String token, BotbyeRequest body) throws JsonProcessingException {
        String url = botbyeConfig.getBotbyeEndpoint() +
                botbyeConfig.getPath() +
                "?" +
                Optional.ofNullable(token).orElse("");

        return new Request.Builder()
                .url(url)
                .post(RequestBody.create(writer.writeValueAsString(body), botbyeConfig.getContentType()))
                .header("Module-Name", BotbyeConfig.getModuleName())
                .header("Module-Version", BotbyeConfig.getModuleVersion())
                .build();
    }
}
