package com.workflow.dao.repository;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@MappedSuperclass
public class Auditable {

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_DATE_TIME", nullable = false, updatable = false)
    private Date createdDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATED_DATE_TIME", nullable = false)
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
