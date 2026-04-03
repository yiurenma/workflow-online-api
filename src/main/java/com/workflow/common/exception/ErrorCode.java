package com.workflow.common.exception;

import java.util.Map;

public class ErrorCode {
    public static final String BAD_REQUEST_INVALID_PARAM = "440000";
    public static final String EXTERNAL_SERVER_ERROR_CODE = "500";

    /** Application-level workflow validation codes (stable for ops / dashboards). */
    public static final String M0001 = "M0001";
    public static final String M0002 = "M0002";
    public static final String M0003 = "M0003";
    public static final String M0004 = "M0004";

    public static final Map<String, String> ERROR_MAPPING = Map.ofEntries(
            Map.entry(M0001, "No or more than one entity setting was found for this application; contact the workflow administrator"),
            Map.entry(M0002, "Duplicate records has been found per request correlation ID "),
            Map.entry(M0003, "Unexpected payload or format in inbound data"),
            Map.entry(M0004, "Duplicate records has been found per isSelfRequest and originWorkflowRecordId ")
    );
}
