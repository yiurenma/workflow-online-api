package com.workflow.common.object;

/** Values persisted on {@code WORKFLOW_TYPE.type}. {@link #DISPATCH} uses the legacy persistence literal for shared-schema compatibility. */
public enum Type {
    CONSUMER,
    /** Same HTTP call as CONSUMER; exceptions are silently swallowed and execution continues to the next step. */
    CONSUMERWITHOUTERROR,
    IFELSE,
    /** Legacy v1 function — read-only support for existing rows; no new records should use this type. */
    FUNCTION,
    FUNCTION_V2,
    /** Identical invocation to FUNCTION_V2 for now; see AD-3 for future differentiation. */
    FUNCTION_V3,
    DISPATCH,
    TRACKING;

    @Override
    public String toString() {
        if (this == DISPATCH) {
            return "MESSAGE";
        }
        return name();
    }
}
