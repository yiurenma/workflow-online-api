package com.workflow.dao.repository;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
        name = "WORKFLOW_ENTITY_AND_LINKING_ID_MAPPING",
        indexes = {
                @Index(name = "idx_entity_linking_entity_logic", columnList = "workflow_entity_setting_id,logic_order"),
                @Index(name = "idx_entity_linking_linking_id", columnList = "linking_id")
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
public class WorkflowEntityAndLinkingIdMapping extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(
            name = "workflow_entity_setting_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_entity_linking_entity_setting")
    )
    private WorkflowEntitySetting workflowEntitySetting;

    @Column(name = "linking_id")
    private String linkingId;

    @Column(name = "logic_order")
    private Integer logicOrder;

    private String remark;
}
