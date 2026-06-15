package com.botbye.protection;

import com.botbye.common.BotbyeError;
import com.botbye.protection.model.BotbyeEvaluateResponse;

/**
 * Builds the fail-open response returned when an evaluation cannot complete (network/SDK error).
 * Decision stays {@code ALLOW} and the error message is attached. Public so callers and framework
 * adapters can produce the same shape for their own short-circuit paths.
 */
public final class FallbackEvaluationResult {
    private FallbackEvaluationResult() {
    }

    public static BotbyeEvaluateResponse create(String message) {
        return new BotbyeEvaluateResponse(new BotbyeError(message));
    }
}
