package com.workflow.common.exception;

public class SamlNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -3837725819329778086L;

    private static final String ERR_MSG = "SAML not found; expected SAML assertion headers on the request.";

    public SamlNotFoundException() {
        super(ERR_MSG);
    }
}
