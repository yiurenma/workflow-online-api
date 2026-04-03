package com.workflow.common.utils;

public class HTTPConstant {
    private HTTPConstant() {
        throw new IllegalStateException("Utility class");
    }

    public static final String CONTENT_TYPE = "Content-Type";
    /** Session-scoped correlation (errors and logging). */
    public static final String SESSION_CORRELATION = "X-Session-Correlation-Id";
    /** Request correlation id (mirrors {@link AppConstant#requestId}). */
    public static final String REQUEST_CORRELATION = "X-Request-Correlation-Id";
    /**
     * Outbound trust-token header for HTTP integrations.
     * Value kept for compatibility with existing workflow templates and downstream services.
     */
    public static final String OUTBOUND_TRUST_TOKEN_HEADER = "X-E2E-Trust-Token";
}
