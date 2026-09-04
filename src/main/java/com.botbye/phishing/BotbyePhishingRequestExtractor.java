package com.botbye.phishing;

/**
 * Maps a framework-specific request object (e.g. {@code HttpServletRequest}, a Ktor/Spring request) to a
 * {@link BotbyePhishingRequestInfo}. A framework SDK describes this once via
 * {@link BotbyePhishingClient#withExtractor}; consumers then pass only their raw request to
 * {@link BotbyePhishingClient#fetchCatcher}.
 *
 * <p>Return a {@link BotbyePhishingRequestInfo} even when the request carries neither header — its
 * {@code origin} and {@code referer} are each allowed to be {@code null}. Returning {@code null}
 * outright is tolerated (treated as "no headers to forward") but not the intended contract.
 */
@FunctionalInterface
public interface BotbyePhishingRequestExtractor<R> {
    BotbyePhishingRequestInfo extract(R request);
}
