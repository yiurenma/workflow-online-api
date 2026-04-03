package com.workflow.common.exception;

import lombok.Builder;

@Builder
public class ErrorInfo{

    private String code;

    public String getCode() {
        return this.code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public Detail getDetail() {
        return this.detail;
    }

    public void setDetail(final Detail detail) {
        this.detail = detail;
    }

    public ErrorInfo() {}

    public ErrorInfo(final String code, final Detail detail) {
        super();
        this.code = code;
        this.detail = detail;
    }

    private Detail detail;
}
