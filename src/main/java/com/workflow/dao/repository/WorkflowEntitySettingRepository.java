package com.workflow.dao.repository;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Hidden
@Tag(name = "DB Repository", description = "Workflow entity settings (WORKFLOW_ENTITY_SETTING)")
public interface WorkflowEntitySettingRepository
        extends JpaRepository<WorkflowEntitySetting, Long>,
        PagingAndSortingRepository<WorkflowEntitySetting, Long>,
        JpaSpecificationExecutor<WorkflowEntitySetting>,
        RevisionRepository<WorkflowEntitySetting, Long, Integer>,
        QuerydslPredicateExecutor<WorkflowEntitySetting> {

    List<WorkflowEntitySetting> findAllByApplicationName(String applicationName);
}
