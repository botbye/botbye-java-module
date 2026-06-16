package com.botbye.common.http;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

/**
 * Default {@link BotbyeHttpClient} backed by OkHttp. Created via {@link #forEvaluate} /
 * {@link #forPhishing}, or wrap a custom {@link OkHttpClient}.
 */
public final class OkHttpBotbyeClient implements BotbyeHttpClient {
    private static final byte[] EMPTY_BODY = new byte[0];

    @SuppressWarnings("KotlinInternalInJava")
    private final OkHttpClient client;

    public OkHttpBotbyeClient(OkHttpClient client) {
        this.client = client;
    }

    /** OkHttp client tuned for the evaluate endpoint (no connection-failure retry). */
    public static OkHttpBotbyeClient forEvaluate(
            int maxRequests,
            int maxRequestsPerHost,
            int maxIdleConnections,
            Duration keepAliveDuration,
            Duration readTimeout,
            Duration writeTimeout,
            Duration connectionTimeout,
            Duration callTimeout
    ) {
        return new OkHttpBotbyeClient(OkHttpClients.create(
                maxRequests, maxRequestsPerHost, maxIdleConnections, keepAliveDuration,
                readTimeout, writeTimeout, connectionTimeout, callTimeout, false));
    }

    /** OkHttp client tuned for the phishing GET (retries idempotent calls on connection failure). */
    public static OkHttpBotbyeClient forPhishing() {
        return new OkHttpBotbyeClient(OkHttpClients.create(
                1500, 1500, 250, Duration.ofSeconds(300),
                Duration.ofSeconds(2), Duration.ofSeconds(2), Duration.ofSeconds(2), Duration.ofSeconds(5),
                true));
    }

    @Override
    public String type() {
        return "okhttp";
    }

    @Override
    public BotbyeHttpResponse call(BotbyeHttpRequest request) throws IOException {
        try (Response response = client.newCall(buildRequest(request)).execute()) {
            return toResponse(response);
        }
    }

    @Override
    public CompletableFuture<BotbyeHttpResponse> callAsync(BotbyeHttpRequest request) {
        CompletableFuture<BotbyeHttpResponse> future = new CompletableFuture<>();

        client.newCall(buildRequest(request)).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try (Response r = response) {
                    future.complete(toResponse(r));
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /** Shuts down the dispatcher's thread pool and evicts pooled connections so no threads leak. */
    @Override
    public void close() {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
        Cache cache = client.cache();
        if (cache != null) {
            try {
                cache.close();
            } catch (IOException ignored) {
                // best-effort cleanup
            }
        }
    }

    private Request buildRequest(BotbyeHttpRequest request) {
        Request.Builder builder = new Request.Builder().url(request.getUrl());

        if ("GET".equalsIgnoreCase(request.getMethod())) {
            builder.get();
        } else {
            MediaType mediaType = request.getContentType() != null ? MediaType.parse(request.getContentType()) : null;
            byte[] body = request.getBody() != null ? request.getBody() : EMPTY_BODY;
            builder.method(request.getMethod(), RequestBody.create(body, mediaType));
        }

        for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
            builder.header(header.getKey(), header.getValue());
        }

        return builder.build();
    }

    private BotbyeHttpResponse toResponse(Response response) throws IOException {
        Map<String, String> headers = new HashMap<>();
        for (String name : response.headers().names()) {
            headers.put(name, response.header(name));
        }

        try (ResponseBody body = response.body()) {
            byte[] bytes = body == null ? EMPTY_BODY : body.bytes();

            return new BotbyeHttpResponse(response.code(), headers, bytes);
        }
    }
}
