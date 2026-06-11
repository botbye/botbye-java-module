package com.botbye.common;

/**
 * Identity of this SDK build. Sent on every BotBye request via the Module-Name / Module-Version
 * headers and embedded in evaluate event payloads. Shared by the protection and phishing clients,
 * so it lives in {@code com.botbye.common} rather than in either client's config.
 */
public final class ModuleInfo {
    public static final String NAME = "Java";
    public static final String VERSION = "3.0.0";

    private ModuleInfo() {
    }
}
