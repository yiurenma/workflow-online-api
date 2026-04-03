package com.workflow.controller;

import com.workflow.common.exception.RestResponseEntityExceptionHandler;
import com.workflow.common.object.security.SecureData;
import com.workflow.common.utils.AppConstant;
import com.workflow.dao.repository.WorkflowEntitySetting;
import com.workflow.dao.repository.WorkflowEntitySettingRepository;
import com.workflow.dao.repository.WorkflowRecord;
import com.workflow.dao.repository.WorkflowRecordRepository;
import com.workflow.service.workflow.WorkflowDispatchService;
import com.workflow.service.workflow.WorkflowRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WorkflowOnlineControllerTest {

    @Mock
    WorkflowEntitySettingRepository workflowEntitySettingRepository;
    @Mock
    WorkflowDispatchService workflowDispatchService;
    @Mock
    WorkflowRecordService workflowRecordService;
    @Mock
    WorkflowRecordRepository workflowRecordRepository;
    @Mock
    SecureData secureData;

    @InjectMocks
    WorkflowOnlineController workflowOnlineController;

    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(workflowOnlineController, "aPort", 8080);
        mockMvc = MockMvcBuilders.standaloneSetup(workflowOnlineController)
                .setControllerAdvice(new RestResponseEntityExceptionHandler())
                .build();
    }

    @Test
    void postWorkflow_delegatesToDispatch() throws Exception {
        WorkflowEntitySetting setting = WorkflowEntitySetting.builder()
                .id(1L)
                .applicationName("AU_PAY_TO")
                .build();
        when(workflowEntitySettingRepository.findAllByApplicationName("AU_PAY_TO"))
                .thenReturn(List.of(setting));
        WorkflowRecord saved = WorkflowRecord.builder().id(99L).build();
        when(workflowRecordService.save(any(WorkflowRecord.class))).thenReturn(saved);
        when(workflowRecordRepository.findIdsByRequestCorrelationIdAndApplicationName(anyString(), anyString()))
                .thenReturn(List.of());
        when(secureData.encrypt(org.mockito.ArgumentMatchers.anyString())).thenReturn("enc");

        mockMvc.perform(post("/api/workflow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .header(AppConstant.requestId, UUID.randomUUID().toString())
                        .param("confirmationNumber", "c1")
                        .param("applicationName", "AU_PAY_TO")
                        .content("{}"))
                .andExpect(status().isOk());

        verify(workflowDispatchService).dispatchFromPersistedRecord(any(WorkflowRecord.class), any());
    }

    @Test
    void postWorkflow_rejectsDuplicateCorrelation() throws Exception {
        when(workflowRecordRepository.findIdsByRequestCorrelationIdAndApplicationName(anyString(), anyString()))
                .thenReturn(List.of(1L));

        mockMvc.perform(post("/api/workflow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .header(AppConstant.requestId, "dup-id")
                        .param("confirmationNumber", "c1")
                        .param("applicationName", "APP")
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(workflowDispatchService, never()).dispatchFromPersistedRecord(any(), any());
    }

    @Test
    void postWorkflow_rejectsWhenEntitySettingMissingOrAmbiguous() throws Exception {
        when(workflowRecordRepository.findIdsByRequestCorrelationIdAndApplicationName(anyString(), anyString()))
                .thenReturn(List.of());
        when(workflowEntitySettingRepository.findAllByApplicationName("NONE")).thenReturn(List.of());

        mockMvc.perform(post("/api/workflow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .header(AppConstant.requestId, UUID.randomUUID().toString())
                        .param("confirmationNumber", "c1")
                        .param("applicationName", "NONE")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postWorkflow_rejectsRetryDuplicateOrigin() throws Exception {
        when(workflowRecordRepository.findIdsByOriginWorkflowRecordId(5L)).thenReturn(List.of(9L));

        mockMvc.perform(post("/api/workflow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .header(AppConstant.requestId, UUID.randomUUID().toString())
                        .param("confirmationNumber", "c1")
                        .param("applicationName", "APP")
                        .param("isSelfRequest", "true")
                        .param("retryOriginWorkflowRecordId", "5")
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(workflowDispatchService, never()).dispatchFromPersistedRecord(any(), any());
    }

    @Test
    void postWorkflow_acceptsXmlBody() throws Exception {
        WorkflowEntitySetting setting = WorkflowEntitySetting.builder()
                .id(1L)
                .applicationName("XML_APP")
                .build();
        when(workflowEntitySettingRepository.findAllByApplicationName("XML_APP")).thenReturn(List.of(setting));
        when(workflowRecordRepository.findIdsByRequestCorrelationIdAndApplicationName(anyString(), anyString()))
                .thenReturn(List.of());
        when(workflowRecordService.save(any(WorkflowRecord.class))).thenAnswer(inv -> inv.getArgument(0));
        when(secureData.encrypt(anyString())).thenReturn("enc");

        mockMvc.perform(post("/api/workflow")
                        .contentType(MediaType.APPLICATION_XML)
                        .header("Content-Type", MediaType.APPLICATION_XML_VALUE)
                        .header(AppConstant.requestId, UUID.randomUUID().toString())
                        .param("confirmationNumber", "c1")
                        .param("applicationName", "XML_APP")
                        .content("<root><k/></root>"))
                .andExpect(status().isOk());

        verify(workflowDispatchService).dispatchFromPersistedRecord(any(WorkflowRecord.class), any());
    }
}
