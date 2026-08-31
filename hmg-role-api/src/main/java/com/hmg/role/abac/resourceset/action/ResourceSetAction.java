package com.hmg.role.abac.resourceset.action;

import com.hmg.role.abac.resourceset.ResourceSet;
import jakarta.persistence.*;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Data
@Table(name = "resource_set_actions")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
public class ResourceSetAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action_name")
    private String actionName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_set_id")
    private ResourceSet resourceSet;

    @Column(name = "is_deleted")
    private boolean deleted;
}
