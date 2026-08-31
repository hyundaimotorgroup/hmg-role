package com.hmg.role.abac.resourceset;

import com.hmg.role.abac.resourceset.action.ResourceSetAction;
import com.hmg.role.abac.resourceset.condition.ResourceSetCondition;
import com.hmg.role.util.entity.AbstractConditionGroup;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Data
@Table(name = "resource_sets")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
public class ResourceSet extends AbstractConditionGroup {

    @Column(name = "resource_set_key")
    private String key;

    @Column(name = "resource_set_name")
    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "resourceSet")
    private List<ResourceSetCondition> conditionGroup;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "resourceSet")
    private List<ResourceSetAction> actions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ResourceSet parent;
}
