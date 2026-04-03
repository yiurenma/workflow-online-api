package com.workflow.service.detail;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow.common.configuration.JacksonConfiguration;
import com.workflow.common.object.WorkflowRuntimePayload;
import com.workflow.dao.repository.WorkflowRuleAndType;
import com.workflow.service.workflow.WorkflowRuleAndTypeService;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class DefaultWorkflowRuntimePayloadImpl implements WorkflowRuntimePayloadInterface {

    @Autowired
    WorkflowRuleAndTypeService workflowRuleAndTypeService;

    @Override
    public WorkflowRuntimePayload getRuntimePayloadWithAsyncEnrichment(
            WorkflowRuntimePayload runtimePayload,
            MultivaluedMap<Integer, List<WorkflowRuleAndType>> bindingsByLogicOrder) throws IOException, ClassNotFoundException {
        List<Integer> keys = new ArrayList<>(bindingsByLogicOrder.keySet());
        Collections.sort(keys);
        for (Integer key : keys) {
            List<CompletableFuture<JSONObject>> futures = new ArrayList<>();
            ObjectMapper om = new JacksonConfiguration().objectMapper();
            log.debug("logic id : {}", key);
            for (List<WorkflowRuleAndType> ruleAndTypes : bindingsByLogicOrder.get(key)) {
                CompletableFuture<JSONObject> future =
                        workflowRuleAndTypeService.executeLinkingOfRuleAndTypeWithAsync(runtimePayload, ruleAndTypes);
                futures.add(future);
            }
            List<JSONObject> branchJsonList = CompletableFuture
                    .allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> {
                        List<JSONObject> jsonObjects = new ArrayList<>();
                        for (CompletableFuture<JSONObject> future : futures) {
                            jsonObjects.add(future.join());
                        }
                        return jsonObjects;
                    })
                    .join();
            for (JSONObject branch : branchJsonList) {
                if (ObjectUtils.isNotEmpty(branch)) {
                    runtimePayload = om.convertValue(
                            om.readerForUpdating(om.readTree(JSONObject.parseObject(om.writeValueAsString(runtimePayload)).toString()))
                                    .readValue(branch.toString()), WorkflowRuntimePayload.class);
                }
            }
        }
        return runtimePayload;
    }

    @Override
    public WorkflowRuntimePayload getRuntimePayloadWithSyncEnrichment(
            WorkflowRuntimePayload runtimePayload,
            MultivaluedMap<Integer, List<WorkflowRuleAndType>> bindingsByLogicOrder) throws IOException, ClassNotFoundException {
        List<Integer> keys = new ArrayList<>(bindingsByLogicOrder.keySet());
        Collections.sort(keys);
        for (Integer key : keys) {
            List<JSONObject> branchJsonList = new ArrayList<>();
            ObjectMapper om = new JacksonConfiguration().objectMapper();
            log.debug("logic id : {}", key);
            for (List<WorkflowRuleAndType> ruleAndTypes : bindingsByLogicOrder.get(key)) {
                JSONObject branch = workflowRuleAndTypeService.executeLinkingOfRuleAndType(runtimePayload, ruleAndTypes);
                branchJsonList.add(branch);
            }
            for (JSONObject branch : branchJsonList) {
                if (ObjectUtils.isNotEmpty(branch)) {
                    runtimePayload = om.convertValue(
                            om.readerForUpdating(om.readTree(JSONObject.parseObject(om.writeValueAsString(runtimePayload)).toString()))
                                    .readValue(branch.toString()), WorkflowRuntimePayload.class);
                }
            }
        }
        return runtimePayload;
    }
}
