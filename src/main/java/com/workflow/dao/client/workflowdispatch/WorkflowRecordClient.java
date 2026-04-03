package com.workflow.dao.client.workflowdispatch;

import com.workflow.common.configuration.httpclient.HttpClientConfiguration;
import com.workflow.common.utils.HTTPConstant;
import com.workflow.dao.repository.WorkflowRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(url = "${workflow.record.sapi.uri}", name = "workflow-record-api",
        configuration = {HttpClientConfiguration.class, WorkflowRecordErrorDecoder.class})
public interface WorkflowRecordClient {

    @PostMapping("/record")
    WorkflowRecord addWorkflowRecord(
            @RequestHeader(HTTPConstant.OUTBOUND_TRUST_TOKEN_HEADER) String token,
            @RequestBody WorkflowRecord record
    );

    @PatchMapping("/record/{id}")
    WorkflowRecord updateWorkflowRecord(
            @RequestHeader(HTTPConstant.OUTBOUND_TRUST_TOKEN_HEADER) String token,
            @PathVariable("id") Long id,
            @RequestBody WorkflowRecord record
    );

    @DeleteMapping("/record/{id}")
    void deleteWorkflowRecord(
            @RequestHeader(HTTPConstant.OUTBOUND_TRUST_TOKEN_HEADER) String token,
            @PathVariable("id") Long id,
            @RequestBody WorkflowRecord record
    );
}
