package com.hmg.role.abac.userset.condition;

import com.hmg.role.util.entity.AbstractConditionOperand;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Data
@Table(name = "user_set_operands")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@EqualsAndHashCode(callSuper = true)
public class UserSetOperand extends AbstractConditionOperand {

    @ManyToOne
    @JoinColumn(name = "user_set_condition_id", referencedColumnName = "id")
    private UserSetCondition userSetCondition;
}
