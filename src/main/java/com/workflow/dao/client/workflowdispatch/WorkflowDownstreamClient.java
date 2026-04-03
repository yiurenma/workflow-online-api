package com.workflow.dao.client.workflowdispatch;

import com.workflow.common.configuration.httpclient.HttpClientConfiguration;
import com.workflow.common.object.WorkflowChannelKind;
import com.workflow.common.utils.HTTPConstant;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@FeignClient(name = "workflow-downstream",
        configuration = {HttpClientConfiguration.class, WorkflowRecordErrorDecoder.class})
public interface WorkflowDownstreamClient {
    @PostMapping(consumes = "application/json", produces = "application/json")
    void postWorkflowDispatch(
            URI uri,
            @RequestHeader(HTTPConstant.OUTBOUND_TRUST_TOKEN_HEADER) String token,
            @RequestHeader(HTTPConstant.REQUEST_CORRELATION) String requestId,
            @RequestParam String confirmationNumber,
            @RequestParam String applicationName,
            @RequestParam("channelKind") WorkflowChannelKind channelKind,
            @RequestParam Boolean isSelfRequest,
            @RequestParam Long retryOriginWorkflowRecordId,
            @RequestBody String requestPayload
    );
}
