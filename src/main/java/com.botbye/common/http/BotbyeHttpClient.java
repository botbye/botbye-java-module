package com.botbye.common.http;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Pluggable HTTP transport. The SDK depends only on this interface; swap in a custom implementation
 * (any HTTP stack) by passing it to the {@code com.botbye.protection.Botbye} /
 * {@code com.botbye.phishing.BotbyePhishingClient} constructors. The default is
 * {@link OkHttpBotbyeClient}.
 */
public interface BotbyeHttpClient {
    /** Identifier of the transport implementation, e.g. {@code "okhttp"}. Sent for diagnostics. */
    String type();

    /** Synchronous call. Throws {@link IOException} on transport failure. */
    BotbyeHttpResponse call(BotbyeHttpRequest request) throws IOException;

    /** Asynchronous call. The future completes exceptionally on transport failure. */
    CompletableFuture<BotbyeHttpResponse> callAsync(BotbyeHttpRequest request);
}
