package com.workflow.common.object;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.workflow.dao.repository.WorkflowEntitySetting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowRuntimePayload {
    String originRequestId;

    @JsonProperty("messageId")
    String workflowInstanceId;

    String requestLocalhostName;
    String requestLocalhostPort;
    String requestSearchKey;
    Long originWorkflowRecordId;

    @JsonProperty("messageType")
    WorkflowChannelKind channelKind;

    @JsonProperty("messageRecord")
    JSONObject embeddedRunSnapshot;

    @JsonMerge
    @JsonProperty("messageInformation")
    JSONObject ingressBody;

    @JsonMerge
    @JsonProperty("messageContactInfo")
    ContactProfile contactProfile;

    @JsonMerge
    @JsonProperty("messageEntitySetting")
    WorkflowEntitySetting workflowEntitySetting;

    @JsonMerge
    @JsonProperty("messageStatus")
    DispatchStepStatus dispatchStepStatus;

    Object reference;
}
