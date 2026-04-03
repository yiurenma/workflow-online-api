package com.workflow.dao.repository;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

@Repository
@Hidden
@Tag(name = "DB Repository", description = "Workflow step types (WORKFLOW_TYPE)")
public interface WorkflowTypeRepository extends
        QuerydslPredicateExecutor<WorkflowType>,
        JpaRepository<WorkflowType, Long>,
        JpaSpecificationExecutor<WorkflowType>,
        RevisionRepository<WorkflowType, Long, Integer> {
}
