package com.workflow.dao.client.trusttoken;

import lombok.Data;

@Data
public class TrustTokenErrorBody {
    private String code;
    private String reason;
    /** Wire field name from the trust-token service error JSON. */
    private String message;
}
