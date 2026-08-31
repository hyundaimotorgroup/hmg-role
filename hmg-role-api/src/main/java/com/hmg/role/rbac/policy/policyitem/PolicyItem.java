package com.hmg.role.rbac.policy.policyitem;

import com.hmg.role.rbac.policy.Policy;
import com.hmg.role.rbac.policy.enums.Effect;
import com.hmg.role.rbac.resourceaction.ResourceAction;
import com.hmg.role.rbac.role.Role;
import com.hmg.role.rbac.scope.Scope;
import jakarta.persistence.*;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.javers.core.metamodel.annotation.ShallowReference;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "rbac_policy_items")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
public class PolicyItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_item_id")
    private Long id;

    @Column(name = "policy_item_key")
    private String policyItemKey;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "effect")
    private Effect effect;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    @ShallowReference
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scope_id")
    @ShallowReference
    private Scope scope;

    @Column(name = "is_deleted")
    private boolean deleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_action_id")
    @ShallowReference
    private ResourceAction resourceAction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id")
    @ShallowReference
    private Policy policy;

    @Column(name = "resource_type_key")
    private String resourceTypeKey;

    @Column(name = "action_name")
    private String actionName;

    @Column(name = "role_key")
    private String roleKey;

    @Column(name = "scope_key")
    private String scopeKey;

    @Column(name = "project_key")
    private String projectKey;

    @Column(name = "policy_key")
    private String policyKey;

    @Column(name = "policy_description")
    private String policyDescription;

    @PrePersist
    @PreUpdate
    void prePersistOrUpdate() {
        if (isInitialized(policy)) {
            this.policyKey = policy.getKey();
            this.policyDescription = policy.getDescription();
            if (isInitialized(policy.getProject())) {
                this.projectKey = policy.getProject().getKey();
            }
        }
        if (isInitialized(resourceAction)) {
            this.actionName = resourceAction.getActionName();
            if (isInitialized(resourceAction.getResourceType())) {
                this.resourceTypeKey = resourceAction.getResourceType().getKey();
            }
        }
        if (isInitialized(role)) {
            this.roleKey = role.getKey();
        }
        if (isInitialized(scope)) {
            this.scopeKey = scope.getKey();
        }
    }

    private static boolean isInitialized(Object obj) {
        return obj != null && Hibernate.isInitialized(obj);
    }
}
