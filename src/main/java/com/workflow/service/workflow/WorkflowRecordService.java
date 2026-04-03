package com.workflow.service.workflow;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow.common.configuration.JacksonConfiguration;
import com.workflow.dao.client.workflowdispatch.WorkflowRecordClient;
import com.workflow.dao.repository.WorkflowEntitySetting;
import com.workflow.dao.repository.WorkflowEntitySettingRepository;
import com.workflow.dao.repository.WorkflowRecord;
import com.workflow.dao.repository.WorkflowRecordRepository;
import com.workflow.service.TrustTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class WorkflowRecordService {

    @Value("${spring.datasource.hikari.read-only:false}")
    Boolean readOnly;
    @Autowired
    WorkflowRecordClient workflowRecordClient;
    @Autowired
    WorkflowRecordRepository workflowRecordRepository;
    @Autowired
    WorkflowEntitySettingRepository workflowEntitySettingRepository;
    @Autowired
    TrustTokenService trustTokenService;

    public WorkflowRecord save(WorkflowRecord record) {
        WorkflowRecord saved;
        if (Boolean.TRUE.equals(readOnly)) {
            String trustToken = getTrustTokenForDefaultServiceAccount(record);
            saved = workflowRecordClient.addWorkflowRecord(trustToken, record);
        } else {
            saved = workflowRecordRepository.save(record);
        }
        return saved;
    }

    public void update(WorkflowRecord record) throws IOException {
        if (Boolean.TRUE.equals(readOnly)) {
            String trustToken = getTrustTokenForDefaultServiceAccount(record);
            workflowRecordClient.updateWorkflowRecord(trustToken, record.getId(), record);
        } else {
            ObjectMapper om = new JacksonConfiguration().objectMapper();
            WorkflowRecord existing = workflowRecordRepository.findById(record.getId()).get();
            workflowRecordRepository.save(
                    om.convertValue(
                            om.readerForUpdating(om.readTree(JSONObject.parseObject(om.writeValueAsString(existing)).toString()))
                                    .readValue(om.readTree(JSONObject.parseObject(om.writeValueAsString(record)).toString())),
                            WorkflowRecord.class
                    )
            );
        }
    }

    public void delete(WorkflowRecord record) {
        if (workflowRecordRepository.existsById(record.getId())) {
            if (Boolean.TRUE.equals(readOnly)) {
                String trustToken = getTrustTokenForDefaultServiceAccount(record);
                workflowRecordClient.deleteWorkflowRecord(trustToken, record.getId(), record);
            } else {
                workflowRecordRepository.delete(record);
            }
        }
    }

    private String getTrustTokenForDefaultServiceAccount(WorkflowRecord record) {
        List<WorkflowEntitySetting> settings = workflowEntitySettingRepository.findAllByApplicationName(
                record.getApplicationName());
        String defaultServiceAccount = "";
        String region = "";
        if (settings.size() == 1) {
            WorkflowEntitySetting setting = settings.get(0);
            defaultServiceAccount = setting.getDefaultServiceAccount();
            region = setting.getRegion();
        }
        return trustTokenService.getTrustToken(defaultServiceAccount, region);
    }
}
