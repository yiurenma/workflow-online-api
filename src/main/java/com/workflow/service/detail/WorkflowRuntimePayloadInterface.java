package com.workflow.service.detail;

import com.workflow.common.object.WorkflowRuntimePayload;
import com.workflow.dao.repository.WorkflowRuleAndType;
import jakarta.ws.rs.core.MultivaluedMap;

import java.io.IOException;
import java.util.List;

public interface WorkflowRuntimePayloadInterface {

    WorkflowRuntimePayload getRuntimePayloadWithAsyncEnrichment(
            WorkflowRuntimePayload runtimePayload,
            MultivaluedMap<Integer, List<WorkflowRuleAndType>> bindingsByLogicOrder) throws IOException, ClassNotFoundException;

    WorkflowRuntimePayload getRuntimePayloadWithSyncEnrichment(
            WorkflowRuntimePayload runtimePayload,
            MultivaluedMap<Integer, List<WorkflowRuleAndType>> bindingsByLogicOrder) throws IOException, ClassNotFoundException;
}
