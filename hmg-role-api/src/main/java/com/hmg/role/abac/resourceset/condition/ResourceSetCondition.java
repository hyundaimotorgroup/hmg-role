package com.hmg.role.abac.resourceset.condition;

import com.hmg.role.abac.resourceset.ResourceSet;
import com.hmg.role.abac.resourceset.exceptions.NullResourceSetOperandException;
import com.hmg.role.abac.resourceset.operand.ResourceSetOperand;
import com.hmg.role.util.entity.AbstractCondition;
import com.hmg.role.util.enums.OperandPosition;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.List;
import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Data
@Table(name = "resource_set_conditions")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
public class ResourceSetCondition extends AbstractCondition {

    @ManyToOne
    @JoinColumn(name = "resource_set_id")
    private ResourceSet resourceSet;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "resourceSetCondition")
    private List<ResourceSetOperand> operands;

    @Transient
    public ResourceSetOperand getLeftOperand() {
        return operands.stream()
                .filter(operand -> operand.getPosition() == OperandPosition.LEFT)
                .findAny()
                .orElseThrow(() -> new NullResourceSetOperandException(OperandPosition.LEFT));
    }

    @Transient
    public ResourceSetOperand getRightOperand() {
        return operands.stream()
                .filter(operand -> operand.getPosition() == OperandPosition.RIGHT)
                .findAny()
                .orElseThrow(() -> new NullResourceSetOperandException(OperandPosition.RIGHT));
    }
}
