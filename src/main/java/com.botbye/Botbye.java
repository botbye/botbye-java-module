package com.botbye;

import com.botbye.model.BotbyeConfig;
import com.botbye.model.BotbyeRequest;
import com.botbye.model.BotbyeResponse;
import com.botbye.model.ConnectionDetails;
import com.botbye.model.Headers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Botbye {
    private static final Logger LOGGER = Logger.getLogger(Botbye.class.getName());
    private BotbyeConfig botbyeConfig = new BotbyeConfig();
    private final ObjectReader reader = new ObjectMapper().reader();
    private final ObjectWriter writer = new ObjectMapper().registerModule(new SimpleModule().addSerializer(Headers.class, new HeadersSerializer())).writer();
    @SuppressWarnings("KotlinInternalInJava")
    private final OkHttpClient client = new OkHttpClient().newBuilder()
            .connectionPool(new ConnectionPool(
                    botbyeConfig.getConnectionPoolSize(),
                    botbyeConfig.getKeepAliveDuration(),
                    botbyeConfig.getKeepAliveDurationTimeUnit())
            )
            .connectTimeout(botbyeConfig.getConnectionTimeout(), botbyeConfig.getConnectionTimeoutUnit())
            .build();

    static {
        LOGGER.setLevel(Level.WARNING);
        LOGGER.addHandler(new ConsoleHandler());
    }

    public Botbye() {
    }

    public Botbye(BotbyeConfig config) {
        botbyeConfig = config;
    }

    public void setConf(BotbyeConfig config) {
        botbyeConfig = config;
    }

    public BotbyeResponse validateRequest(String token, ConnectionDetails connectionDetails, Headers headers) {
        return validateRequest(token, connectionDetails, headers, Collections.emptyList());
    }

    public BotbyeResponse validateRequest(String token, ConnectionDetails connectionDetails, Headers headers, List<String> customFields) {
        validateServerKey();

        BotbyeRequest body = createBotbyeRequestBody(token, headers, connectionDetails, customFields);

        try {
            Request request = createRequest(body);
            Response response = client.newCall(request).execute();
            return handleResponse(response);
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
            return new BotbyeResponse();
        }
    }

    public CompletableFuture<BotbyeResponse> validateRequestAsync(String token, ConnectionDetails connectionDetails, Headers headers) {
        return validateRequestAsync(token, connectionDetails, headers, Collections.emptyList());
    }

    public CompletableFuture<BotbyeResponse> validateRequestAsync(String token, ConnectionDetails connectionDetails, Headers headers, List<String> customFields) {
        validateServerKey();
        BotbyeRequest body = createBotbyeRequestBody(token, headers, connectionDetails, customFields);

        CompletableFuture<BotbyeResponse> future = new CompletableFuture<>();
        try {
            Request request = createRequest(body);
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    future.complete(handleResponse(response));
                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    LOGGER.warning(e.getMessage());
                    future.complete(new BotbyeResponse());
                }
            });
            return future;
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
            future.complete(new BotbyeResponse());
            return future;
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
            LOGGER.warning(e.getMessage());
            return new BotbyeResponse();
        }
    }

    private BotbyeRequest createBotbyeRequestBody(String token, Headers headers, ConnectionDetails connectionDetails, List<String> customFields) {
        return new BotbyeRequest(token, botbyeConfig.getServerKey(), headers, connectionDetails, customFields);
    }

    private Request createRequest(BotbyeRequest body) throws JsonProcessingException {
        String url = String.format("%s%s", botbyeConfig.getBotbyeEndpoint(), botbyeConfig.getPath());

        return new Request.Builder()
                .url(url)
                .post(RequestBody.create(writer.writeValueAsString(body), botbyeConfig.getContentType()))
                .addHeader("Module-Name", BotbyeConfig.moduleName)
                .addHeader("Module-Version", BotbyeConfig.moduleVersion)
                .build();
    }
}