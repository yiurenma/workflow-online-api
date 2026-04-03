package com.workflow.dao.repository;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Repository
@Hidden
@Tag(name = "DB Repository", description = "Workflow runs (WORKFLOW_RECORD)")
public interface WorkflowRecordRepository extends
        QuerydslPredicateExecutor<WorkflowRecord>,
        JpaRepository<WorkflowRecord, Long>,
        JpaSpecificationExecutor<WorkflowRecord>,
        RevisionRepository<WorkflowRecord, Long, Integer>,
        QuerydslBinderCustomizer<QWorkflowRecord> {

    @Query(nativeQuery = true, value = ""
            + "SELECT id FROM workflow_record "
            + "WHERE origin_workflow_record_id = ?1 ")
    List<Long> findIdsByOriginWorkflowRecordId(Long originWorkflowRecordId);

    @Query(nativeQuery = true, value = ""
            + "SELECT id FROM workflow_record "
            + "WHERE request_correlation_id = ?1 AND application_name = ?2")
    List<Long> findIdsByRequestCorrelationIdAndApplicationName(String requestCorrelationId, String applicationName);

    @Override
    default void customize(QuerydslBindings bindings, QWorkflowRecord root) {
        bindings.bind(root.createdDateTime).all((path, value) -> {
            Iterator<? extends Date> it = value.iterator();
            return Optional.ofNullable(path.between(it.next(), it.next()));
        });

        bindings.bind(root.lastModifiedDateTime).all((path, value) -> {
            Iterator<? extends Date> it = value.iterator();
            return Optional.ofNullable(path.between(it.next(), it.next()));
        });
    }
}
