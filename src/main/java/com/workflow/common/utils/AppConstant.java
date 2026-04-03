package com.workflow.common.utils;

public class AppConstant {
    private AppConstant() {
        throw new IllegalStateException("Utility class");
    }

    public static final String VARIABLE_BEGIN_STRING = "<<<";
    public static final String VARIABLE_END_STRING = ">>>";
    /** Discriminator value for legacy numeric account formatting rules in workflow templates. */
    public static final String LEGACY_NUMERIC_ACCOUNT_TYPE = "LEGACY_NUMERIC_ACCOUNT";
    /** When {@link com.workflow.common.utils.Functions#formatAccountNumber} receives this entity code, extended masking rules apply. */
    public static final String WORKFLOW_ACCOUNT_FORMAT_ENTITY_CODE = "WORKFLOW_DEMO_ENTITY";
    public static final String ib2bTokenCache = "ib2b_token";
    public static final String workflowRuleBindingLinkingIdCache = "workflowRuleBindingLinkingId";
    public static final String workflowEntityLinkCache = "workflowEntityLink";
    public static final String HEADER = "header";
    public static final String requestId = "X-Request-Correlation-Id";
    public static final String DEFAULT = "DEFAULT";
}
