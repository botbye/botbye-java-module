package com.botbye.protection;

import com.botbye.protection.model.BotbyeEvaluateResponse;
import com.botbye.protection.model.BotbyeEvent;
import java.util.concurrent.CompletableFuture;

/**
 * Explicit-event evaluation surface, independent of any framework request type.
 *
 * <p>Consumers that build {@link BotbyeEvent}s themselves (rather than relying on a
 * {@link BotbyeRequestExtractor}) should depend on this interface instead of the raw {@code Botbye}
 * / {@code Botbye<?>} type — it carries only the members that do not involve the request type
 * parameter {@code R}, so there is no unused generic to spell out (and no unchecked-raw-type warning)
 * at call sites.
 *
 * <p>{@link Botbye} implements it for every {@code R}; {@code new Botbye(config)} yields an instance
 * usable as a {@link BotbyeEvaluator}.
 */
public interface BotbyeEvaluator {
    /** Send a fully-built event for risk evaluation. Fails open: returns ALLOW + error on failure. */
    BotbyeEvaluateResponse evaluate(BotbyeEvent event);

    /** Asynchronous variant of {@link #evaluate(BotbyeEvent)}. */
    CompletableFuture<BotbyeEvaluateResponse> evaluateAsync(BotbyeEvent event);

    /** Replace the runtime configuration (endpoint / server key) of this client. */
    void setConf(BotbyeConfig config);
}
