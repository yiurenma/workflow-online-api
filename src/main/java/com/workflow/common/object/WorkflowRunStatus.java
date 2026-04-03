package com.workflow.common.object;

/**
 * Values persisted in {@code WORKFLOW_RECORD.overall_status} and related channel fields.
 * Constant names are stable for existing database rows.
 */
public enum WorkflowRunStatus {
    INITIATION,

    GI_SUCCESS,
    GI_FAIL,

    SM_SUCCESS,
    SM_FAIL,

    FB_PARTIAL_SUCCESS,
    FB_ALL_SUCCESS,
    FB_NOT_AVAILABLE,
    FB_ALL_FAIL,
    FB_UNKNOWN,

    RETRY_ALL_FAIL,
}
