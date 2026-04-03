package com.workflow.service.workflow;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.workflow.common.configuration.JacksonConfiguration;
import com.workflow.common.configuration.httpclient.RestTemplateConfiguration;
import com.workflow.common.object.WorkflowRuntimePayload;
import com.workflow.common.utils.AppConstant;
import com.workflow.common.utils.HTTPConstant;
import com.workflow.common.utils.Tools;
import com.workflow.dao.repository.WorkflowType;
import com.workflow.service.TrustTokenService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Slf4j
public class CommonClientService {

    @Autowired
    RestTemplateConfiguration restTemplateConfiguration;

    @Autowired
    TrustTokenService trustTokenService;
    @Value("${workflow.http.internal-host-marker}")
    String internalHostMarker;

    public WorkflowRuntimePayload sending(WorkflowType workflowType, WorkflowRuntimePayload runtimePayload) {

        ResponseEntity<JSONObject> jsonObjectResponseEntity;
        try {
            HashMap<String, String> requestHeaders = new HashMap<>();
            if (workflowType.getHttpRequestHeaders() != null) {
                requestHeaders = new JacksonConfiguration().objectMapper().readValue(
                        Tools.replaceVariables(
                                new String(Base64.getDecoder().decode(workflowType.getHttpRequestHeaders()), StandardCharsets.UTF_8),
                                runtimePayload,
                                AppConstant.VARIABLE_BEGIN_STRING,
                                AppConstant.VARIABLE_END_STRING
                        ),
                        HashMap.class
                );
            }
            MultiValueMap<String, String> requestHeadersMap = new LinkedMultiValueMap<>();
            for (Map.Entry<String, String> entry : requestHeaders.entrySet())
                requestHeadersMap.put(entry.getKey(), Collections.singletonList(entry.getValue()));
            if (ObjectUtils.isNotEmpty(requestHeadersMap.getFirst("Authentication"))) {
                requestHeadersMap.add(HTTPConstant.OUTBOUND_TRUST_TOKEN_HEADER, trustTokenService.getTrustToken(Objects.requireNonNull(requestHeadersMap.getFirst("Authentication")), runtimePayload.getWorkflowEntitySetting().getRegion()));
            } else if (ObjectUtils.isNotEmpty(runtimePayload.getWorkflowEntitySetting().getDefaultServiceAccount())) {
                requestHeadersMap.add(HTTPConstant.OUTBOUND_TRUST_TOKEN_HEADER, trustTokenService.getTrustToken(runtimePayload.getWorkflowEntitySetting().getDefaultServiceAccount(), runtimePayload.getWorkflowEntitySetting().getRegion()));
            }

            Object requestBody = new Object();
            if (workflowType.getHttpRequestBody() != null) {
                requestBody =
                        JSON.parse(
                                Tools.replaceVariables(
                                        new String(Base64.getDecoder().decode(workflowType.getHttpRequestBody()), StandardCharsets.UTF_8),
                                        runtimePayload,
                                        AppConstant.VARIABLE_BEGIN_STRING,
                                        AppConstant.VARIABLE_END_STRING
                                )
                        );
            }
            String httpRequestUrlWithQueryParameter = new String(Base64.getDecoder().decode(workflowType.getHttpRequestUrlWithQueryParameter()), StandardCharsets.UTF_8);
            if (Strings.isNotEmpty(httpRequestUrlWithQueryParameter)) {
                httpRequestUrlWithQueryParameter =
                        Tools.replaceVariables(
                                httpRequestUrlWithQueryParameter,
                                runtimePayload,
                                AppConstant.VARIABLE_BEGIN_STRING,
                                AppConstant.VARIABLE_END_STRING
                        );
                if (new URL(httpRequestUrlWithQueryParameter).getHost().contains(internalHostMarker)) {
                    httpRequestUrlWithQueryParameter = new String(Base64.getDecoder().decode(workflowType.getInternalHttpRequestUrlWithQueryParameter()), StandardCharsets.UTF_8);
                    httpRequestUrlWithQueryParameter =
                            Tools.replaceVariables(
                                    httpRequestUrlWithQueryParameter,
                                    runtimePayload,
                                    AppConstant.VARIABLE_BEGIN_STRING,
                                    AppConstant.VARIABLE_END_STRING
                            );
                }
            }
            jsonObjectResponseEntity = restTemplateConfiguration.getRestTemplate().exchange(
                    Tools.replaceVariables(
                            httpRequestUrlWithQueryParameter,
                            runtimePayload,
                            AppConstant.VARIABLE_BEGIN_STRING,
                            AppConstant.VARIABLE_END_STRING
                    ),
                    HttpMethod.valueOf(workflowType.getHttpRequestMethod()),
                    new HttpEntity<>(requestBody,
                            requestHeadersMap),
                    JSONObject.class
            );
        } catch (Exception e) {
            log.error("stop outbound HTTP step because content is not correct : {}", e);
            return null;
        }

        runtimePayload.setReference(jsonObjectResponseEntity.getBody());
        return runtimePayload;
    }
}
