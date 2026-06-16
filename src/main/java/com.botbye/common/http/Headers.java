package com.botbye.common.http;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Multi-value HTTP header carrier. Framework adapters wrap their native (multi-value) headers in this
 * type; the SDK owns the multi-value &rarr; flat-string normalization (lowercased keys, comma-joined
 * values) at serialization time via {@link HeadersSerializer}, so adapters never flatten themselves.
 */
public final class Headers implements Serializable {
    private final Map<String, List<String>> headers;

    public Headers(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }
}
