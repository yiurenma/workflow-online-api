package com.workflow.common.object;

/** Values persisted on {@code WORKFLOW_TYPE.type}. {@link #DISPATCH} uses the legacy persistence literal for shared-schema compatibility. */
public enum Type {
    CONSUMER, IFELSE, FUNCTION, FUNCTION_V2, DISPATCH, TRACKING;

    @Override
    public String toString() {
        if (this == DISPATCH) {
            return "MESSAGE";
        }
        return name();
    }
}
