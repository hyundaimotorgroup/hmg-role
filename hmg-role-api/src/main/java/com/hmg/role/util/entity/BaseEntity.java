package com.hmg.role.util.entity;

import jakarta.persistence.*;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.ZonedDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@MappedSuperclass
@EqualsAndHashCode
// TODO: @EnableJpaAuditing not wired; AuditorAware conflicts with async threads and multi-auth
// paths; set createdBy/updatedBy manually in services
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedBy
    @Column(name = "created_by")
    protected String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    protected ZonedDateTime createdAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    protected String updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    protected ZonedDateTime updatedAt;
}
