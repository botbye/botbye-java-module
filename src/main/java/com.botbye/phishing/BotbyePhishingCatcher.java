package com.botbye.phishing;

public final class BotbyePhishingCatcher {
    private final String format;
    private final String innerPngUrl;
    private final boolean skipExecution;

    private BotbyePhishingCatcher(String format, String innerPngUrl, boolean skipExecution) {
        this.format = format;
        this.innerPngUrl = innerPngUrl;
        this.skipExecution = skipExecution;
    }

    public static BotbyePhishingCatcher png() {
        return new BotbyePhishingCatcher("png", null, true);
    }

    public static BotbyePhishingCatcher svg(String innerPngUrl) {
        return svg(innerPngUrl, true);
    }

    public static BotbyePhishingCatcher svg(String innerPngUrl, boolean skipExecution) {
        if (innerPngUrl == null || innerPngUrl.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "[BotBye] BotbyePhishingCatcher.svg: innerPngUrl must be a non-blank absolute http(s) URL");
        }

        return new BotbyePhishingCatcher("svg", innerPngUrl, skipExecution);
    }

    String getFormat() {
        return format;
    }

    String getInnerPngUrl() {
        return innerPngUrl;
    }

    boolean isSkipExecution() {
        return skipExecution;
    }

    boolean isSvg() {
        return "svg".equals(format);
    }
}
