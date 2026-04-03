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
@Tag(name = "DB Repository", description = "Workflow rule-to-type bindings")
public interface WorkflowRuleBindingRepository extends
        QuerydslPredicateExecutor<WorkflowRuleBinding>,
        JpaRepository<WorkflowRuleBinding, Long>,
        JpaSpecificationExecutor<WorkflowRuleBinding>,
        RevisionRepository<WorkflowRuleBinding, Long, Integer> {
    List<WorkflowRuleBinding> getAllByLinkingId(String linkingId);
}