package com.hmg.role.abac.resourceset.operand;

import com.hmg.role.abac.resourceset.condition.ResourceSetCondition;
import com.hmg.role.util.entity.AbstractConditionOperand;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Data
@Table(name = "resource_set_operands")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
public class ResourceSetOperand extends AbstractConditionOperand {

    @ManyToOne
    @JoinColumn(name = "resource_set_condition_id")
    private ResourceSetCondition resourceSetCondition;
}
