package com.hmg.role.abac.policy;

import com.hmg.role.abac.resourceset.action.ResourceSetAction;
import com.hmg.role.abac.scope.AbacScope;
import com.hmg.role.abac.userset.UserSet;
import com.hmg.role.rbac.policy.enums.Effect;
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
import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.javers.core.metamodel.annotation.ShallowReference;

@Data
@Table(name = "abac_policy_items")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
public class AbacPolicyItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_item_id")
    private Long id;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "effect")
    private Effect effect;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_set_id")
    private UserSet userSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scope_id")
    @ShallowReference
    private AbacScope scope;

    @Column(name = "is_deleted")
    private boolean deleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_set_action_id")
    private ResourceSetAction resourceSetAction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id")
    private AbacPolicy policy;

    // keys are flattened because the N+1 is stubbornly hard to remove
    // and use of nativeQuery = true is forbidden
    // Ugly? Yes. Performant? Unfortunately yes

    @Column(name = "project_key")
    private String projectKey;

    @Column(name = "resource_set_key")
    private String resourceSetKey;

    @Column(name = "user_set_key")
    private String userSetKey;

    @Column(name = "action_name")
    private String actionName;

    @Column(name = "scope_key")
    private String scopeKey;

    @Column(name = "policy_key")
    private String policyKey;

    @PrePersist
    @PreUpdate
    private void updateDenormalizedFields() {
        if (userSet != null) {
            this.userSetKey = userSet.getKey();
        }
        if (scope != null) {
            this.scopeKey = scope.getKey();
        }
        if (policy != null) {
            this.policyKey = policy.getKey();
            if (policy.getProject() != null) {
                this.projectKey = policy.getProject().getKey();
            }
        }
        if (resourceSetAction != null) {
            this.actionName = resourceSetAction.getActionName();
            if (resourceSetAction.getResourceSet() != null) {
                this.resourceSetKey = resourceSetAction.getResourceSet().getKey();
            }
        }
    }
}
