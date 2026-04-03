package com.workflow.dao.client.ib2b;

import lombok.Data;

@Data
public class Ib2bTokenExceptionResponse {
    private String code;
    private String reason;
    private String message;
}
