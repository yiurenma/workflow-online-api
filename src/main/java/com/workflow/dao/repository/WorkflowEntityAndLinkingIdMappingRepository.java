package com.workflow.dao.repository;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Repository
@RequestMapping(value = "/workflow/")
@Hidden
@Tag(name = "DB Repository", description = "Workflow entity → linking-id steps (shared schema)")
public interface WorkflowEntityAndLinkingIdMappingRepository extends
        QuerydslPredicateExecutor<WorkflowEntityAndLinkingIdMapping>,
        JpaRepository<WorkflowEntityAndLinkingIdMapping, Long>,
        JpaSpecificationExecutor<WorkflowEntityAndLinkingIdMapping>,
        RevisionRepository<WorkflowEntityAndLinkingIdMapping, Long, Integer> {

    List<WorkflowEntityAndLinkingIdMapping> findAllByWorkflowEntitySettingId(long workflowEntitySettingId);
}
