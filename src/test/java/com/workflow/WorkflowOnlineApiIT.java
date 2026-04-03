package com.workflow;

import com.workflow.common.utils.AppConstant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@Transactional
@EnabledIfEnvironmentVariable(named = "IT_WORKFLOW_APPLICATION_NAME", matches = ".+")
class WorkflowOnlineApiIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void postWorkflow_acceptsWithExistingEntitySetting() throws Exception {
        String applicationName = System.getenv("IT_WORKFLOW_APPLICATION_NAME");
        String confirmation = Optional.ofNullable(System.getenv("IT_WORKFLOW_CONFIRMATION_NUMBER"))
                .filter(s -> !s.isBlank())
                .orElse("itest-confirmation");

        mockMvc.perform(post("/api/workflow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .header(AppConstant.requestId, UUID.randomUUID().toString())
                        .param("confirmationNumber", confirmation)
                        .param("applicationName", applicationName)
                        .content("{}"))
                .andExpect(status().isOk());
    }
}
