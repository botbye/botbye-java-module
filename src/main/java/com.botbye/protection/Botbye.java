package com.botbye.protection;

import com.botbye.common.BotbyeError;
import com.botbye.common.ErrorClassifier;
import com.botbye.common.OkHttpClients;
import com.botbye.protection.model.BotbyeEvaluateResponse;
import com.botbye.protection.model.BotbyeEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

/**
 * Evaluate client (Level 1/2 bot &amp; risk scoring). Requires a server key and runs an init
 * handshake on construction. Phishing image tracking lives in
 * {@link com.botbye.phishing.BotbyePhishingClient}.
 */
public class Botbye {
    private static final Logger LOGGER = Logger.getLogger(Botbye.class.getName());

    static {
        LOGGER.setLevel(Level.WARNING);
        LOGGER.addHandler(new ConsoleHandler());
    }

    private final ObjectMapper mapper = new ObjectMapper();
    private final ObjectReader reader = mapper.reader();

    private BotbyeConfig botbyeConfig;
    private String evaluateBaseUrl;
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
        evaluateBaseUrl = config.getBotbyeEndpoint() + "/api/v1/protect/evaluate";

        client = OkHttpClients.create(
                botbyeConfig.getMaxRequests(),
                botbyeConfig.getMaxRequestsPerHost(),
                botbyeConfig.getMaxIdleConnections(),
                botbyeConfig.getKeepAliveDuration(),
                botbyeConfig.getReadTimeout(),
                botbyeConfig.getWriteTimeout(),
                botbyeConfig.getConnectionTimeout(),
                botbyeConfig.getCallTimeout(),
                false
        );
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
            return new BotbyeEvaluateResponse(new BotbyeError(ErrorClassifier.classify(e)));
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
                    future.complete(new BotbyeEvaluateResponse(new BotbyeError(ErrorClassifier.classify(e))));
                }
            });
        } catch (IOException e) {
            LOGGER.warning("[BotBye] exception occurred: " + e.getMessage());
            future.complete(new BotbyeEvaluateResponse(new BotbyeError(ErrorClassifier.classify(e))));
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
            return new BotbyeEvaluateResponse(new BotbyeError(ErrorClassifier.classify(e)));
        }
    }

    private Request buildEvaluateHttpRequest(String url, ObjectWriter writer, BotbyeEvent event) throws com.fasterxml.jackson.core.JsonProcessingException {
        return new Request.Builder()
                .url(url)
                .post(RequestBody.create(writer.writeValueAsBytes(event), botbyeConfig.getContentType()))
                .header("Module-Name", BotbyeConfig.getModuleName())
                .header("Module-Version", BotbyeConfig.getModuleVersion())
                .build();
    }

}
