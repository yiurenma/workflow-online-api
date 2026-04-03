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
    /** Outbound trust / bearer token header used for integration Feign clients. */
    public static final String X_E2E_TRUST_TOKEN = "X-E2E-Trust-Token";
}
