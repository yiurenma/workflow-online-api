package com.workflow.common.exception;

import lombok.Data;

import java.util.List;

@Data
public class GeneralError {

    private List<ErrorInfo> errorInfo;
    private String requestCorrelation;
    private String sessionCorrelation;

    public GeneralError() {}

    public GeneralError(final String requestCorrelation, final String sessionCorrelation) {
        super();
        this.requestCorrelation = requestCorrelation;
        this.sessionCorrelation = sessionCorrelation;
    }

    public GeneralError(final List<ErrorInfo> errorInfo, final String requestCorrelation, final String sessionCorrelation) {
        super();
        this.errorInfo = errorInfo;
        this.requestCorrelation = requestCorrelation;
        this.sessionCorrelation = sessionCorrelation;
    }

}
