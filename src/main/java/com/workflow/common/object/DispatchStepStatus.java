package com.workflow.common.object;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DispatchStepStatus {
    private List<String> emailStatusList;
    private List<String> smsStatusList;
    private List<String> pushNotificationStatusList;
    private List<String> pushNotificationDetailStatusList;
    private List<String> providerDescriptionList;
}
