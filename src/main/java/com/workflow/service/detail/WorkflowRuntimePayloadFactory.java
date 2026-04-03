package com.workflow.service.detail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkflowRuntimePayloadFactory {

    @Autowired
    DefaultWorkflowRuntimePayloadImpl defaultWorkflowRuntimePayload;

    public WorkflowRuntimePayloadInterface getInstance(String applicationName) {
        return defaultWorkflowRuntimePayload;
    }
}
