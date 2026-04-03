package com.workflow.dao.repository;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Table(
        name = "WORKFLOW_RECORD",
        indexes = {
                @Index(name = "idx_workflow_record_app_created", columnList = "application_name,CREATED_DATE_TIME"),
                @Index(name = "idx_workflow_record_corr_app", columnList = "request_correlation_id,application_name"),
                @Index(name = "idx_workflow_record_origin", columnList = "origin_workflow_record_id"),
                @Index(name = "idx_workflow_record_status_created", columnList = "overall_status,CREATED_DATE_TIME"),
                @Index(name = "idx_workflow_record_tracking_created", columnList = "tracking_number,CREATED_DATE_TIME")
        }
)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@DynamicUpdate
public class WorkflowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "application_name")
    private String applicationName;
    @Column(name = "request_correlation_id")
    private String requestCorrelationId;
    private String transactionConfirmationNumber;
    private String workflowLinkingId;
    @Column(name = "tracking_number")
    private String trackingNumber;
    @Column(columnDefinition = "TEXT")
    private String workflowTransactionDetails;
    @Column(columnDefinition = "TEXT")
    private String workflowResponseFromProvider;
    private String workflowProvider;
    private String customerId;
    @Column(name = "overall_status")
    private String overallStatus;
    private String smsStatus;
    private String emailStatus;
    private String pushNotificationStatus;
    private String pushNotificationDetailStatus;
    @Column(columnDefinition = "TEXT")
    private String providerDescription;
    @Column(nullable = true)
    private Integer retryTimes;
    @Column(name = "origin_workflow_record_id", nullable = true)
    private Long originWorkflowRecordId;
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "origin_workflow_record_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_workflow_record_origin"),
            insertable = false,
            updatable = false
    )
    private WorkflowRecord originWorkflowRecord;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_DATE_TIME", nullable = false, updatable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ssXXX")
    private Date createdDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATED_DATE_TIME", nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ssXXX")
    private Date lastModifiedDateTime;

    @PrePersist
    protected void onCreate() {
        lastModifiedDateTime = createdDateTime = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDateTime = new Date();
    }
}
