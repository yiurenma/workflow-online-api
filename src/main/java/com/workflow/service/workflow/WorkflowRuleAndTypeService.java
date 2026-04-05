package com.workflow.service.workflow;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow.common.configuration.JacksonConfiguration;
import com.workflow.common.object.FunctionObject;
import com.workflow.common.object.ParameterObject;
import com.workflow.common.object.WorkflowRunStatus;
import com.workflow.common.object.WorkflowRuntimePayload;
import com.workflow.common.object.security.SecureData;
import com.workflow.common.utils.AppConstant;
import com.workflow.common.utils.Tools;
import com.workflow.dao.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.workflow.common.object.Type.*;
import static com.workflow.common.utils.AppConstant.workflowEntityLinkingIdMappingCache;
import static com.workflow.common.utils.AppConstant.workflowRuleAndTypeLinkingIdCache;
import static com.workflow.service.workflow.WorkflowDispatchService.ruleAndTypesFullyMatch;

@Service
@Slf4j
public class WorkflowRuleAndTypeService {

    @Autowired
    CommonClientService commonClientService;
    @Autowired
    WorkflowRuleAndTypeRepository workflowRuleAndTypeRepository;
    @Autowired
    WorkflowEntityAndLinkingIdMappingRepository workflowEntityAndLinkingIdMappingRepository;
    @Autowired
    SecureData secureData;

    @Async
    public CompletableFuture<JSONObject> executeLinkingOfRuleAndTypeWithAsync(
            WorkflowRuntimePayload runtimePayload,
            List<WorkflowRuleAndType> ruleAndTypes) throws JsonProcessingException, ClassNotFoundException {
        return CompletableFuture.completedFuture(executeLinkingOfRuleAndType(runtimePayload, ruleAndTypes));
    }

    public JSONObject executeLinkingOfRuleAndType(
            WorkflowRuntimePayload runtimePayload,
            List<WorkflowRuleAndType> ruleAndTypes) throws JsonProcessingException, ClassNotFoundException {
        ObjectMapper om = new JacksonConfiguration().objectMapper();
        JSONObject subWorkflowRuntimePayload = null;
        if (ruleAndTypesFullyMatch(runtimePayload, ruleAndTypes)) {
            WorkflowType wt = ruleAndTypes.get(0).getWorkflowType();
            if (CONSUMER.toString().equals(wt.getType())) {
                log.info("{} execute because of : {}", CONSUMER, wt.getRemark());
                subWorkflowRuntimePayload =
                        om.readValue(
                                Tools.replaceVariables(
                                        new String(Base64.getDecoder().decode(wt.getTrackingNumberSchemaInHttpResponse())),
                                        commonClientService.sending(wt, runtimePayload),
                                        AppConstant.VARIABLE_BEGIN_STRING,
                                        AppConstant.VARIABLE_END_STRING
                                ),
                                JSONObject.class
                        );
            }
            if (CONSUMERWITHOUTERROR.toString().equals(wt.getType())) {
                log.info("{} execute because of : {}", CONSUMERWITHOUTERROR, wt.getRemark());
                try {
                    subWorkflowRuntimePayload =
                            om.readValue(
                                    Tools.replaceVariables(
                                            new String(Base64.getDecoder().decode(wt.getTrackingNumberSchemaInHttpResponse())),
                                            commonClientService.sending(wt, runtimePayload),
                                            AppConstant.VARIABLE_BEGIN_STRING,
                                            AppConstant.VARIABLE_END_STRING
                                    ),
                                    JSONObject.class
                            );
                } catch (Exception e) {
                    log.warn("{} step suppressed exception (execution continues): {}", CONSUMERWITHOUTERROR, e.getMessage());
                }
            }
            if (IFELSE.toString().equals(wt.getType())) {
                log.info("{} execute because of : {}", IFELSE, wt.getRemark());
                subWorkflowRuntimePayload =
                        om.readValue(
                                Tools.replaceVariables(
                                        new String(Base64.getDecoder().decode(wt.getElseLogic())),
                                        runtimePayload,
                                        AppConstant.VARIABLE_BEGIN_STRING,
                                        AppConstant.VARIABLE_END_STRING
                                ),
                                JSONObject.class
                        );
            }
            if (DISPATCH.toString().equals(wt.getType())) {
                log.info("{} execute because of : {}", DISPATCH, wt.getRemark());
                WorkflowRecord branchRecord = WorkflowRecord.builder().build();
                try {
                    branchRecord.setWorkflowProvider(wt.getProvider());
                    runtimePayload.setWorkflowInstanceId(UUID.randomUUID().toString());
                    branchRecord.setWorkflowLinkingId(ruleAndTypes.get(0).getLinkingId());
                    branchRecord.setWorkflowTransactionDetails(
                            secureData.encrypt(JSONObject.parseObject(om.writeValueAsString(runtimePayload)).toString()));
                    branchRecord.setApplicationName(runtimePayload.getWorkflowEntitySetting().getApplicationName());
                    try {
                        branchRecord.setTrackingNumber(
                                Tools.replaceVariables(
                                        new String(Base64.getDecoder().decode(wt.getTrackingNumberSchemaInHttpResponse())),
                                        commonClientService.sending(wt, runtimePayload),
                                        AppConstant.VARIABLE_BEGIN_STRING,
                                        AppConstant.VARIABLE_END_STRING
                                )
                        );
                        if (StringUtils.isNotEmpty(branchRecord.getTrackingNumber())) {
                            branchRecord.setOverallStatus(WorkflowRunStatus.SM_SUCCESS.toString());
                        } else {
                            branchRecord.setOverallStatus(WorkflowRunStatus.SM_FAIL.toString());
                        }
                        branchRecord.setWorkflowResponseFromProvider(
                                secureData.encrypt(runtimePayload.getReference() != null
                                        ? runtimePayload.getReference().toString() : ""));
                    } catch (Exception e) {
                        log.warn("Exception stack", e);
                        branchRecord.setOverallStatus(WorkflowRunStatus.SM_FAIL.toString());
                        branchRecord.setWorkflowResponseFromProvider(e.toString());
                    }
                } catch (Exception e) {
                    log.warn("Exception during outbound dispatch step : {}, exception details as : {}", branchRecord, e.toString());
                    log.warn("Exception stack", e);
                }
                if ("SYSTEM".equals(wt.getProvider())) {
                    subWorkflowRuntimePayload = null;
                } else {
                    subWorkflowRuntimePayload = JSONObject.parseObject(om.writeValueAsString(branchRecord));
                }
            }
            if (FUNCTION.toString().equals(wt.getType())) {
                log.info("{} execute because of : {}", FUNCTION, wt.getRemark());
                FunctionObject function = JSONObject.parseObject(new String(Base64.getDecoder().decode(wt.getElseLogic())), FunctionObject.class);
                Class<?> foundClass = Class.forName(function.getClassName());
                Method foundMethod = null;
                Method[] methods = foundClass.getDeclaredMethods();
                for (int i = 0; i < methods.length; i++) {
                    if (function.getMethodName().equals(methods[i].getName())
                            && function.getInputParameterList().size() == methods[i].getParameterCount()) {
                        function.getInputParameterList().sort((s1, s2) -> s1.getParameterOrder().compareTo(s2.getParameterOrder()));
                        Class[] paramTypes = new Class[function.getInputParameterList().size()];
                        Object[] paramValues = new Object[function.getInputParameterList().size()];
                        for (int j = 0; j < function.getInputParameterList().size(); j++) {
                            paramTypes[j] = Class.forName(function.getInputParameterList().get(j).getParameterClass());
                            paramValues[j] =
                                    Tools.replaceVariables(
                                            function.getInputParameterList().get(j).getParameterValue(),
                                            runtimePayload,
                                            AppConstant.VARIABLE_BEGIN_STRING,
                                            AppConstant.VARIABLE_END_STRING
                                    );
                        }
                        try {
                            foundMethod = foundClass.getMethod(function.getMethodName(), paramTypes);
                            subWorkflowRuntimePayload =
                                    om.readValue(
                                            Tools.replaceVariables(
                                                    function.getOutputParameter().toString(),
                                                    JSONObject.parseObject(om.writeValueAsString(foundMethod.invoke(foundClass.newInstance(), paramValues))),
                                                    AppConstant.VARIABLE_BEGIN_STRING,
                                                    AppConstant.VARIABLE_END_STRING
                                            ),
                                            JSONObject.class
                                    );
                            break;
                        } catch (NoSuchMethodException e) {
                            continue;
                        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            if (FUNCTION_V2.toString().equals(wt.getType())) {
                log.info("{} execute because of : {}", FUNCTION_V2, wt.getRemark());
                FunctionObject function = JSONObject.parseObject(new String(Base64.getDecoder().decode(wt.getElseLogic())), FunctionObject.class);
                Class<?> foundClass = Class.forName(function.getClassName());
                Method[] methods = foundClass.getDeclaredMethods();
                for (int i = 0; i < methods.length; i++) {
                    if (function.getMethodName().equals(methods[i].getName())
                            && function.getInputParameterList().size() == methods[i].getParameterCount()) {
                        function.getInputParameterList().sort(Comparator.comparing(ParameterObject::getParameterOrder));

                        Class[] paramTypes = new Class[function.getInputParameterList().size()];
                        Object[] paramValues = new Object[function.getInputParameterList().size()];
                        for (int j = 0; j < function.getInputParameterList().size(); j++) {
                            paramTypes[j] = Class.forName(function.getInputParameterList().get(j).getParameterClass());
                            paramValues[j] =
                                    Tools.replaceVariables(
                                            function.getInputParameterList().get(j).getParameterValue(),
                                            runtimePayload,
                                            AppConstant.VARIABLE_BEGIN_STRING,
                                            AppConstant.VARIABLE_END_STRING
                                    );
                        }
                        try {
                            Method foundMethod = foundClass.getMethod(function.getMethodName(), paramTypes);
                            subWorkflowRuntimePayload =
                                    om.readValue(
                                            Tools.replaceVariables(
                                                    function.getOutputParameter().toString(),
                                                    JSONObject.parseObject(
                                                            om.writeValueAsString(
                                                                    WorkflowRuntimePayload.builder()
                                                                            .reference(foundMethod.invoke(foundClass.newInstance(), paramValues))
                                                                            .build())),
                                                    AppConstant.VARIABLE_BEGIN_STRING,
                                                    AppConstant.VARIABLE_END_STRING
                                            ),
                                            JSONObject.class
                                    );
                            break;
                        } catch (NoSuchMethodException e) {
                            continue;
                        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            // TODO (AD-3): FUNCTION_V3 uses a different invocation style from FUNCTION_V2;
            //  implement the differentiated logic when the invocation contract is finalised.
            if (FUNCTION_V3.toString().equals(wt.getType())) {
                log.info("{} execute because of : {}", FUNCTION_V3, wt.getRemark());
                FunctionObject function = JSONObject.parseObject(new String(Base64.getDecoder().decode(wt.getElseLogic())), FunctionObject.class);
                Class<?> foundClass = Class.forName(function.getClassName());
                Method[] methods = foundClass.getDeclaredMethods();
                for (int i = 0; i < methods.length; i++) {
                    if (function.getMethodName().equals(methods[i].getName())
                            && function.getInputParameterList().size() == methods[i].getParameterCount()) {
                        function.getInputParameterList().sort(Comparator.comparing(ParameterObject::getParameterOrder));

                        Class[] paramTypes = new Class[function.getInputParameterList().size()];
                        Object[] paramValues = new Object[function.getInputParameterList().size()];
                        for (int j = 0; j < function.getInputParameterList().size(); j++) {
                            paramTypes[j] = Class.forName(function.getInputParameterList().get(j).getParameterClass());
                            paramValues[j] =
                                    Tools.replaceVariables(
                                            function.getInputParameterList().get(j).getParameterValue(),
                                            runtimePayload,
                                            AppConstant.VARIABLE_BEGIN_STRING,
                                            AppConstant.VARIABLE_END_STRING
                                    );
                        }
                        try {
                            Method foundMethod = foundClass.getMethod(function.getMethodName(), paramTypes);
                            subWorkflowRuntimePayload =
                                    om.readValue(
                                            Tools.replaceVariables(
                                                    function.getOutputParameter().toString(),
                                                    JSONObject.parseObject(
                                                            om.writeValueAsString(
                                                                    WorkflowRuntimePayload.builder()
                                                                            .reference(foundMethod.invoke(foundClass.newInstance(), paramValues))
                                                                            .build())),
                                                    AppConstant.VARIABLE_BEGIN_STRING,
                                                    AppConstant.VARIABLE_END_STRING
                                            ),
                                            JSONObject.class
                                    );
                            break;
                        } catch (NoSuchMethodException e) {
                            continue;
                        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        return subWorkflowRuntimePayload;
    }

    @Cacheable(key = "#linkingId", value = workflowRuleAndTypeLinkingIdCache)
    public List<WorkflowRuleAndType> findRuleAndTypesByLinkingId(String linkingId) {
        return workflowRuleAndTypeRepository.getAllByLinkingId(linkingId);
    }

    @Cacheable(key = "#workflowEntitySettingId", value = workflowEntityLinkingIdMappingCache)
    public List<WorkflowEntityAndLinkingIdMapping> findEntityLinkingMappingsBySettingId(Long workflowEntitySettingId) {
        return workflowEntityAndLinkingIdMappingRepository.findAllByWorkflowEntitySettingId(workflowEntitySettingId);
    }
}
