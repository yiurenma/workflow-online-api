package com.workflow.service.detail;

import com.workflow.common.object.WorkflowRuntimePayload;
import com.workflow.dao.repository.WorkflowRuleBinding;
import jakarta.ws.rs.core.MultivaluedMap;

import java.io.IOException;
import java.util.List;

public interface WorkflowRuntimePayloadInterface {
    WorkflowRuntimePayload getTransactionDetails(
            WorkflowRuntimePayload runtimePayload,
            MultivaluedMap<Integer, List<WorkflowRuleBinding>> bindingsByLogicOrder) throws IOException, ClassNotFoundException;

    WorkflowRuntimePayload getTransactionDetailsWithoutAsync(
            WorkflowRuntimePayload runtimePayload,
            MultivaluedMap<Integer, List<WorkflowRuleBinding>> bindingsByLogicOrder) throws IOException, ClassNotFoundException;
}
