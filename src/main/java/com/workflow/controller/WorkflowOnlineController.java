package com.workflow.controller;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow.common.configuration.JacksonConfiguration;
import com.workflow.common.exception.BaseErrorException;
import com.workflow.common.exception.ErrorCode;
import com.workflow.common.exception.GeneralError;
import com.workflow.common.exception.GeneralWarning;
import com.workflow.common.object.WorkflowRunStatus;
import com.workflow.common.object.WorkflowRuntimePayload;
import com.workflow.common.object.WorkflowChannelKind;
import com.workflow.common.object.security.SecureData;
import com.workflow.common.utils.AppConstant;
import com.workflow.dao.repository.WorkflowEntitySetting;
import com.workflow.dao.repository.WorkflowEntitySettingRepository;
import com.workflow.dao.repository.WorkflowRecord;
import com.workflow.dao.repository.WorkflowRecordRepository;
import com.workflow.service.workflow.WorkflowRecordService;
import com.workflow.service.workflow.WorkflowDispatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

import static com.workflow.common.utils.HTTPConstant.CONTENT_TYPE;


@Slf4j
@RestController
@RequestMapping(value = "/api/")
@Tag(name = "Online workflow", description = "POST /api/workflow — async dispatch entry")
@Validated
public class WorkflowOnlineController {

    @Value("${server.port}")
    Integer aPort;

    @Autowired
    WorkflowDispatchService workflowDispatchService;

    @Autowired
    WorkflowEntitySettingRepository workflowEntitySettingRepository;

    @Autowired
    WorkflowRecordService workflowRecordService;

    @Autowired
    WorkflowRecordRepository workflowRecordRepository;

    @Autowired
    SecureData secureData;

    @Operation(
            summary = "Accept a workflow dispatch request (JSON or XML body) and run the configured async pipeline",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Request accepted; enrichment may resolve transaction and contact details per workflow configuration"
                    ),
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful or Successful with warning",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = GeneralWarning.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Business Error",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = GeneralError.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "System Error",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = GeneralError.class
                                    )
                            )
                    )
            }
    )
    @PostMapping(
            value = {"/workflow"},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public ResponseEntity<Void> postWorkflow(

            @RequestHeader("Content-Type")
            @Parameter(
                    example = "application/json",
                    required = true,
                    description = """
                            """
            )
            @NotNull
            MediaType contentType,

            @Valid
            @RequestHeader(AppConstant.requestId)
            @Parameter(
                    example = "cc25d99e-afbb-4c71-9b34-6b4a5050d29e",
                    required = true,
                    description = """
                            Request correlation id for duplicate run detection
                            """
            )
            @NotNull
            String requestId,

            @RequestParam(required = true)
            @Parameter(
                    example = "GXZ001TL4X or payee~~62430~~543122999612240700",
                    required = true,
                    description = """
                            Search key / confirmation value stored with the workflow run for lookups and idempotency context
                            """
            )
            @NotNull
            String confirmationNumber,

            @RequestParam(required = true)
            @Parameter(
                    example = "AU_PAY_TO",
                    required = true,
                    description = """
                            Application name registered in workflow entity settings for this dispatch flow
                            """
            )
            @NotNull
            String applicationName,

            @RequestParam(name = "channelKind", required = false)
            @Parameter(
                    example = "SMS",
                    required = false,
                    description = """
                            Optional channel kind hint for downstream dispatch (e.g. SMS, Email) when retrying or supplementing a run.
                            """)
            WorkflowChannelKind channelKind,

            @RequestParam(required = false, defaultValue = "false")
            @Parameter(
                    required = false,
                    hidden = true,
                    description = """
                            if it is a self request (eg: joint account case, then no need to validate the request id duplication)
                            """)
            Boolean isSelfRequest,

            @RequestParam(required = false)
            @Parameter(
                    required = false,
                    hidden = true,
                    description = """
                            for retry and it is a internal field
                            """)
            Long retryOriginWorkflowRecordId,

            @RequestBody(
                    required = false
            )
            @Parameter(
                    description = """
                            JSON String or XML String
                            """
            )
            @Valid String requestPayload
    ) throws IOException, ClassNotFoundException {
        ObjectMapper om = new JacksonConfiguration().objectMapper();
        String body = requestPayload;
        if (MediaType.APPLICATION_XML_VALUE.equalsIgnoreCase(contentType.toString())
                && StringUtils.isNotEmpty(body)) {
            body = XML.toJSONObject(body).toString();
        }
        if (!isSelfRequest && workflowRecordRepository.findIdsByRequestCorrelationIdAndApplicationName(requestId, applicationName).size() > 0) {
            throw BaseErrorException.withErrorCodeAndErrorDetails(
                    ErrorCode.M0002,
                    ErrorCode.ERROR_MAPPING.get(ErrorCode.M0002) + requestId
            );
        }
        if (isSelfRequest
                && retryOriginWorkflowRecordId != null
                && workflowRecordRepository.findIdsByOriginWorkflowRecordId(retryOriginWorkflowRecordId).size() > 0) {
            throw BaseErrorException.withErrorCodeAndErrorDetails(
                    ErrorCode.M0004,
                    ErrorCode.ERROR_MAPPING.get(ErrorCode.M0004) + retryOriginWorkflowRecordId
            );
        }
        List<WorkflowEntitySetting> settings = workflowEntitySettingRepository.findAllByApplicationName(applicationName);
        if (settings.size() == 1) {
            WorkflowRecord record = WorkflowRecord.builder().build();
            record.setApplicationName(applicationName);
            record.setTransactionConfirmationNumber(confirmationNumber);
            record.setRequestCorrelationId(requestId);
            record.setOverallStatus(WorkflowRunStatus.INITIATION.toString());
            record.setOriginWorkflowRecordId(retryOriginWorkflowRecordId);

            WorkflowRuntimePayload runtimePayload = new WorkflowRuntimePayload();
            runtimePayload.setOriginRequestId(requestId);
            runtimePayload.setWorkflowInstanceId(UUID.randomUUID().toString());
            runtimePayload.setWorkflowEntitySetting(settings.get(0));
            runtimePayload.setChannelKind(channelKind);
            runtimePayload.setIngressBody(JSONObject.parseObject(body));
            runtimePayload.setOriginWorkflowRecordId(retryOriginWorkflowRecordId);
            runtimePayload.setRequestLocalhostName(InetAddress.getLoopbackAddress().getHostName());
            runtimePayload.setRequestLocalhostPort(aPort.toString());
            runtimePayload.setRequestSearchKey(confirmationNumber);
            record.setWorkflowTransactionDetails(secureData.encrypt(JSONObject.parseObject(om.writeValueAsString(runtimePayload)).toString()));
            record = workflowRecordService.save(record);

            if (settings.get(0).isAsyncMode()) {
                workflowDispatchService.dispatchFromPersistedRecord(record, runtimePayload);
            } else {
                workflowDispatchService.dispatchFromPersistedRecordSync(record, runtimePayload);
            }
        } else {
            throw BaseErrorException.withErrorCodeAndErrorDetails(
                    ErrorCode.M0001,
                    ErrorCode.ERROR_MAPPING.get(ErrorCode.M0001)
            );
        }

        return ResponseEntity.ok().header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
    }
}
