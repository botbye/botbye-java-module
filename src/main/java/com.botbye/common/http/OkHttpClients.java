package com.botbye.common.http;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

/** Builds a configured {@link OkHttpClient}. OkHttp is confined to the {@code common.http} package. */
public final class OkHttpClients {
    private OkHttpClients() {
    }

    public static OkHttpClient create(
            int maxRequests,
            int maxRequestsPerHost,
            int maxIdleConnections,
            Duration keepAliveDuration,
            Duration readTimeout,
            Duration writeTimeout,
            Duration connectionTimeout,
            Duration callTimeout,
            boolean retryOnConnectionFailure
    ) {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(maxRequests);
        dispatcher.setMaxRequestsPerHost(maxRequestsPerHost);

        return new OkHttpClient().newBuilder()
                .retryOnConnectionFailure(retryOnConnectionFailure)
                .dispatcher(dispatcher)
                .connectionPool(new ConnectionPool(
                        maxIdleConnections,
                        keepAliveDuration.toMillis(),
                        TimeUnit.MILLISECONDS)
                )
                .readTimeout(readTimeout)
                .callTimeout(callTimeout)
                .connectTimeout(connectionTimeout)
                .writeTimeout(writeTimeout)
                .build();
    }
}
