package com.workflow.service.workflow;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow.common.configuration.JacksonConfiguration;
import com.workflow.common.object.WorkflowRunStatus;
import com.workflow.common.object.WorkflowRuntimePayload;
import com.workflow.common.object.security.SecureData;
import com.workflow.dao.repository.WorkflowEntityAndLinkingIdMapping;
import com.workflow.dao.repository.WorkflowRecord;
import com.workflow.dao.repository.WorkflowRuleAndType;
import com.workflow.service.detail.WorkflowRuntimePayloadFactory;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.workflow.common.object.Type.*;

@Service
@Slf4j
public class WorkflowDispatchService {

    @Autowired
    WorkflowRuleAndTypeService workflowRuleAndTypeService;
    @Autowired
    WorkflowRuntimePayloadFactory workflowRuntimePayloadFactory;
    @Autowired
    SecureData secureData;
    @Autowired
    WorkflowRecordService workflowRecordService;
    @Value("${async.enrichInformation}")
    boolean enrichInformationAsync;
    @Value("${async.dispatchChannels}")
    boolean dispatchChannelsAsync;

    /**
     * Synchronous dispatch — used when {@code asyncMode=false} on WorkflowEntitySetting.
     * Enrichment and dispatch complete before the caller returns; the HTTP response is only
     * sent after the full pipeline has finished.
     */
    public void dispatchFromPersistedRecordSync(WorkflowRecord executionRecord,
                                                WorkflowRuntimePayload runtimePayload)
            throws IOException, ClassNotFoundException {
        runDispatchPipeline(executionRecord, runtimePayload);
    }

    @Async
    public void dispatchFromPersistedRecord(WorkflowRecord executionRecord,
                                            WorkflowRuntimePayload runtimePayload)
            throws IOException, ClassNotFoundException {
        runDispatchPipeline(executionRecord, runtimePayload);
    }

    private void runDispatchPipeline(WorkflowRecord executionRecord,
                                     WorkflowRuntimePayload runtimePayload)
            throws IOException, ClassNotFoundException {
        List<WorkflowEntityAndLinkingIdMapping> entityLinks =
                workflowRuleAndTypeService.findEntityLinkingMappingsBySettingId(runtimePayload.getWorkflowEntitySetting().getId());
        log.info("There are {} steps for {}",
                entityLinks.size(),
                runtimePayload.getWorkflowEntitySetting().getApplicationName()
        );

        MultivaluedMap<Integer, List<WorkflowRuleAndType>> outboundBindingsByOrder = new MultivaluedHashMap<>();
        MultivaluedMap<Integer, List<WorkflowRuleAndType>> enrichmentBindingsByOrder = new MultivaluedHashMap<>();
        for (int i = 0; i < entityLinks.size(); i++) {
            List<WorkflowRuleAndType> bindings =
                    workflowRuleAndTypeService.findRuleAndTypesByLinkingId(entityLinks.get(i).getLinkingId());
            if (!bindings.isEmpty()) {
                WorkflowRuleAndType first = bindings.get(0);
                if (CONSUMER.toString().equals(first.getWorkflowType().getType())
                        || CONSUMERWITHOUTERROR.toString().equals(first.getWorkflowType().getType())
                        || IFELSE.toString().equals(first.getWorkflowType().getType())
                        || FUNCTION.toString().equalsIgnoreCase(first.getWorkflowType().getType())
                        || FUNCTION_V2.toString().equalsIgnoreCase(first.getWorkflowType().getType())
                        || FUNCTION_V3.toString().equalsIgnoreCase(first.getWorkflowType().getType())) {
                    enrichmentBindingsByOrder.add(entityLinks.get(i).getLogicOrder(), bindings);
                }
                if (DISPATCH.toString().equals(first.getWorkflowType().getType())) {
                    outboundBindingsByOrder.add(entityLinks.get(i).getLogicOrder(), bindings);
                }
            }
        }

        try {
            if (enrichInformationAsync) {
                runtimePayload = workflowRuntimePayloadFactory
                        .getInstance(runtimePayload.getWorkflowEntitySetting().getApplicationName())
                        .getRuntimePayloadWithAsyncEnrichment(runtimePayload, enrichmentBindingsByOrder);
            } else {
                runtimePayload = workflowRuntimePayloadFactory
                        .getInstance(runtimePayload.getWorkflowEntitySetting().getApplicationName())
                        .getRuntimePayloadWithSyncEnrichment(runtimePayload, enrichmentBindingsByOrder);
            }
        } catch (Exception e) {
            log.error("exception when gather information : {}", e.toString());
            executionRecord.setOverallStatus(WorkflowRunStatus.GI_FAIL.toString());
        }

        ObjectMapper om = new JacksonConfiguration().objectMapper();
        executionRecord.setWorkflowTransactionDetails(
                secureData.encrypt(JSONObject.parseObject(om.writeValueAsString(runtimePayload)).toString()));
        executionRecord.setCustomerId(
                runtimePayload.getContactProfile() != null ? runtimePayload.getContactProfile().getCustomerId() : null);
        if (!executionRecord.getOverallStatus().equals(WorkflowRunStatus.GI_FAIL.toString())) {
            executionRecord.setOverallStatus(WorkflowRunStatus.GI_SUCCESS.toString());
        }
        workflowRecordService.save(executionRecord);

        if (dispatchChannelsAsync) {
            dispatchOutboundChannelsAsync(runtimePayload, executionRecord, outboundBindingsByOrder);
        } else {
            dispatchOutboundChannelsSync(runtimePayload, executionRecord, outboundBindingsByOrder);
        }
    }

    public void dispatchOutboundChannelsAsync(WorkflowRuntimePayload runtimePayload,
                                              WorkflowRecord executionRecord,
                                              MultivaluedMap<Integer, List<WorkflowRuleAndType>> outboundBindingsByOrder)
            throws JsonProcessingException, ClassNotFoundException {
        List<Integer> keys = new ArrayList<>(outboundBindingsByOrder.keySet());
        Collections.sort(keys);
        for (Integer key : keys) {
            List<CompletableFuture<JSONObject>> futures = new ArrayList<>();
            for (List<WorkflowRuleAndType> bindings : outboundBindingsByOrder.get(key)) {
                CompletableFuture<JSONObject> future =
                        workflowRuleAndTypeService.executeLinkingOfRuleAndTypeWithAsync(runtimePayload, bindings);
                futures.add(future);
            }
            List<JSONObject> branchResults = CompletableFuture
                    .allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> {
                        List<JSONObject> jsonObjects = new ArrayList<>();
                        for (CompletableFuture<JSONObject> future : futures) {
                            jsonObjects.add(future.join());
                        }
                        return jsonObjects;
                    })
                    .join();
            for (JSONObject branchResult : branchResults) {
                if (ObjectUtils.isNotEmpty(branchResult)) {
                    mergeBranchResultIntoRecord(executionRecord, branchResult);
                }
            }
        }
    }

    public void dispatchOutboundChannelsSync(WorkflowRuntimePayload runtimePayload,
                                             WorkflowRecord executionRecord,
                                             MultivaluedMap<Integer, List<WorkflowRuleAndType>> outboundBindingsByOrder)
            throws JsonProcessingException, ClassNotFoundException {
        List<Integer> keys = new ArrayList<>(outboundBindingsByOrder.keySet());
        Collections.sort(keys);
        for (Integer key : keys) {
            List<JSONObject> branchResults = new ArrayList<>();
            for (List<WorkflowRuleAndType> bindings : outboundBindingsByOrder.get(key)) {
                JSONObject branchResult =
                        workflowRuleAndTypeService.executeLinkingOfRuleAndType(runtimePayload, bindings);
                branchResults.add(branchResult);
            }
            for (JSONObject branchResult : branchResults) {
                if (ObjectUtils.isNotEmpty(branchResult)) {
                    mergeBranchResultIntoRecord(executionRecord, branchResult);
                }
            }
        }
    }

    private void mergeBranchResultIntoRecord(WorkflowRecord parentRecord, JSONObject branchResult)
            throws JsonProcessingException {
        ObjectMapper om = new JacksonConfiguration().objectMapper();
        WorkflowRecord child = WorkflowRecord.builder().build();
        child.setRequestCorrelationId(parentRecord.getRequestCorrelationId());
        child.setTransactionConfirmationNumber(parentRecord.getTransactionConfirmationNumber());
        child.setCustomerId(parentRecord.getCustomerId());
        child.setOriginWorkflowRecordId(parentRecord.getOriginWorkflowRecordId());
        child = om.convertValue(
                om.readerForUpdating(om.readTree(JSONObject.parseObject(om.writeValueAsString(child)).toString()))
                        .readValue(branchResult.toString()), WorkflowRecord.class);
        workflowRecordService.save(child);
        workflowRecordService.delete(parentRecord);
    }

    public static boolean ruleAndTypesFullyMatch(Object payloadObject, List<WorkflowRuleAndType> bindings) {
        int ruleMatchCount = 0;
        for (WorkflowRuleAndType binding : bindings) {
            String ruleKey = binding.getWorkflowRule().getKey();
            log.debug("ruleKey is : {}", ruleKey);

            try {
                String transactionDetailsString = new JacksonConfiguration().objectMapper().writeValueAsString(payloadObject);
                Configuration configuration = Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS).build();
                JSONArray parseResult = JSONArray.parseArray(
                        JsonPath.using(configuration).parse(transactionDetailsString).read(ruleKey).toString());
                log.debug("Json Parse Result is : {} ", parseResult);

                if (parseResult.isEmpty()) {
                    break;
                } else {
                    ruleMatchCount++;
                }
            } catch (JsonProcessingException e) {
                log.error("Json Parse Error happened.", e);
            }
        }
        return ruleMatchCount == bindings.size();
    }
}
