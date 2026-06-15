package com.botbye.phishing;

/**
 * Extracts the {@code Origin} header value from a framework-specific request object. A framework SDK
 * describes this once via {@link BotbyePhishingClient#withExtractor}; consumers then pass only their
 * raw request to {@link BotbyePhishingClient#fetchImage}. Return {@code null} when no {@code Origin}
 * is present.
 */
@FunctionalInterface
public interface BotbyePhishingRequestExtractor<R> {
    String extractOrigin(R request);
}
