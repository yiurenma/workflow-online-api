package com.workflow.common.exception;

import lombok.Data;

import java.util.List;

@Data
public class GeneralWarning {

    private List<ErrorInfo> warningInfo;
    private String requestCorrelation;
    private String sessionCorrelation;

    public GeneralWarning() {}

    public GeneralWarning(final String requestCorrelation, final String sessionCorrelation) {
        super();
        this.requestCorrelation = requestCorrelation;
        this.sessionCorrelation = sessionCorrelation;
    }

    public GeneralWarning(final List<ErrorInfo> errorInfo, final String requestCorrelation, final String sessionCorrelation) {
        super();
        this.warningInfo = errorInfo;
        this.requestCorrelation = requestCorrelation;
        this.sessionCorrelation = sessionCorrelation;
    }
}
