package com.botbye.phishing;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BotbyePhishingRequestInfo implements Serializable {
    private final String origin;
    private final String referer;
    private final Map<String, List<String>> query;

    public BotbyePhishingRequestInfo(String origin) {
        this(origin, null);
    }

    public BotbyePhishingRequestInfo(String origin, String referer) {
        this(origin, referer, null);
    }

    public BotbyePhishingRequestInfo(String origin, String referer, Map<String, List<String>> query) {
        this.origin = origin;
        this.referer = referer;
        this.query = query == null ? Collections.emptyMap() : query;
    }

    public String getOrigin() {
        return origin;
    }

    public String getReferer() {
        return referer;
    }

    public Map<String, List<String>> getQuery() {
        return query;
    }
}
