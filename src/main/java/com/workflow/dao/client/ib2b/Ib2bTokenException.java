package com.workflow.dao.client.ib2b;

import lombok.Data;

@Data
public class Ib2bTokenException extends RuntimeException{
    private final String errorCode;
    private final String errorDetail;

    public Ib2bTokenException(String message, String errorCode, String errorDetail) {
        super(message);
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
    }
}
