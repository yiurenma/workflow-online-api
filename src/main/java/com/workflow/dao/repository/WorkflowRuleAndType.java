package com.workflow.dao.repository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
        name = "WORKFLOW_RULE_AND_TYPE_MAPPING",
        indexes = {
                @Index(name = "idx_rule_type_mapping_linking_id", columnList = "linking_id")
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class WorkflowRuleAndType extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "linking_id")
    private String linkingId;

    @ManyToOne
    @JoinColumn(
            name = "workflow_rule_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_workflow_rule_id", value = ConstraintMode.CONSTRAINT)
    )
    private WorkflowRule workflowRule;

    @ManyToOne
    @JoinColumn(
            name = "workflow_type_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_workflow_type_id", value = ConstraintMode.CONSTRAINT)
    )
    private WorkflowType workflowType;
}
