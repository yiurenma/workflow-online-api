package com.workflow.dao.client.trusttoken;

public class TrustTokenClientException extends RuntimeException {

    private final String errorCode;
    private final String errorDetail;

    public TrustTokenClientException(String description, String errorCode, String errorDetail) {
        super(description);
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDetail() {
        return errorDetail;
    }
}
