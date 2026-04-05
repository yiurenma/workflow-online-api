package com.workflow.dao.repository;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;

@Entity
@Table(
        name = "WORKFLOW_ENTITY_SETTING",
        indexes = {
                @Index(name = "idx_workflow_entity_setting_application_name", columnList = "application_name")
        }
)
@DynamicUpdate
@Audited
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@EntityListeners(AuditingEntityListener.class)
public class WorkflowEntitySetting extends Auditable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    @Comment("Primary key, auto-generated")
    private Long id;

    @Comment("Name of the application or client using this workflow")
    @Column(name = "application_name", nullable = false)
    private String applicationName;

    @Column(nullable = true)
    @Comment("Whether retry is enabled for this entity")
    private boolean retry;
    @Column(nullable = true)
    @Comment("Whether tracking is enabled for this entity")
    private boolean tracking;

    @ManyToOne
    @JoinColumn(name = "workflow_type_id")
    @Comment("Primary workflow type for this entity")
    private WorkflowType trackingServiceProviderActionId;
    @ManyToOne
    @JoinColumn(name = "workflow_type_id_2")
    @Comment("Secondary workflow type (e.g. fallback)")
    private WorkflowType trackingServiceProviderActionId2;

    @Column(nullable = true)
    @Comment("Enterprise identity / integration identifier")
    private String eimId;
    @Column(nullable = true)
    @Comment("Default service account for API calls")
    private String defaultServiceAccount;
    @Column(nullable = true)
    @Comment("Region or environment (e.g. prod, dev)")
    private String region;
    @Column(nullable = true)
    @Comment("Whether this entity setting is active")
    private boolean enabled;

    @Column(nullable = true)
    @Comment("Whether duplicate-record errors should be ignored")
    private Boolean ignoreDuplicateRecordError;

    @Column(length = 10000)
    @Comment("Retry configuration payload (JSON/text)")
    private String retryProperties;

    @Column(nullable = true, columnDefinition = "boolean default true")
    @Comment("Whether enrichment and dispatch execute asynchronously (true) or synchronously (false)")
    private boolean asyncMode = true;

    @Column(columnDefinition = "TEXT")
    @Comment("Base64-encoded workflow UI map (plugin list, uiMap, uiMapList)")
    private String workflow;
}
