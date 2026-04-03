package com.workflow.controller;

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
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
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
        StandaloneMockMvcBuilder builder = MockMvcBuilders.standaloneSetup(workflowOnlineController);
        mockMvc = builder.build();
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
}
