package com.workflow.common.exception;

import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
public class Detail {

    private String cause;

    public String getCause() {
        return this.cause;
    }

    public Detail(final String cause) {
        super();
        this.cause = null == cause ? "" : cause.replace("<", "&lt;").replace(">", "&gt;").replace("&", "&amp;").replace("'", "&apos;");
    }

    public void setCause(final String cause) {
        this.cause = cause;
    }
}
