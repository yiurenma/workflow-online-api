package com.workflow.dao.repository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;

@Entity
@Table(name = "WORKFLOW_TYPE")
@DynamicUpdate
@Audited
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class WorkflowType extends Auditable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Comment("Primary key, auto-generated")
    private Long id;

    @Comment("Provider identifier for display/reference only; not used in workflow logic")
    private String provider;
    @Comment("Type/category of the workflow (e.g. notification, tracking)")
    private String type;
    @Comment("Free-text description or notes")
    private String remark;
    @Comment("Comma-separated error codes that trigger retry logic")
    private String retryErrorCodes;

    @Column(length = 50000)
    @Comment("JSON or script for fallback logic when the rule does not match")
    private String elseLogic;

    @Comment("HTTP method (GET, POST, etc.) for backend API calls")
    private String httpRequestMethod;
    @Column(length = 10000)
    @Comment("Full URL including query parameters for the backend API")
    private String httpRequestUrlWithQueryParameter;
    @Column(length = 10000)
    @Comment("Internal URL for backend API (e.g. internal service endpoint)")
    private String internalHttpRequestUrlWithQueryParameter;
    @Column(length = 10000)
    @Comment("JSON or string for HTTP headers to send with the request")
    private String httpRequestHeaders;
    @Column(length = 10000)
    @Comment("Request body template for the backend API")
    private String httpRequestBody;
    @Column(length = 10000)
    @Comment("JSON schema or path to extract tracking number from HTTP response")
    private String trackingNumberSchemaInHttpResponse;
}
