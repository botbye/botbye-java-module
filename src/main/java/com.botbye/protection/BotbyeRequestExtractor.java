package com.botbye.protection;

import com.botbye.protection.model.BotbyeRequestInfo;

/**
 * Maps a framework-specific request object (e.g. {@code HttpServletRequest}, a Ktor/Spring request)
 * to a {@link BotbyeRequestInfo}. A framework SDK describes this once via
 * {@link Botbye#withExtractor}; consumers then pass only their raw request to the {@code evaluate*}
 * methods.
 */
@FunctionalInterface
public interface BotbyeRequestExtractor<R> {
    BotbyeRequestInfo extract(R request);
}
