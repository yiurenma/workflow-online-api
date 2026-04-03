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
    public static final String trustTokenCache = "trust_token";
    public static final String workflowRuleAndTypeLinkingIdCache = "workflow_rule_and_type_linking_id";
    public static final String workflowEntityLinkingIdMappingCache = "workflow_entity_linking_id_mapping";
    public static final String HEADER = "header";
    public static final String requestId = "X-Request-Correlation-Id";
    public static final String DEFAULT = "DEFAULT";
}
