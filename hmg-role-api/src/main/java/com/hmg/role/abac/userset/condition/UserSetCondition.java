package com.hmg.role.abac.userset.condition;

import com.hmg.role.abac.user.exception.NullUserOperandException;
import com.hmg.role.abac.userset.UserSet;
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
@Table(name = "user_set_conditions")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
public class UserSetCondition extends AbstractCondition {

    @ManyToOne
    @JoinColumn(name = "user_set_id")
    private UserSet userSet;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "userSetCondition")
    private List<UserSetOperand> operands;

    @Transient
    public UserSetOperand getLeftOperand() {
        return operands.stream()
                .filter(operand -> operand.getPosition() == OperandPosition.LEFT)
                .findAny()
                .orElseThrow(() -> new NullUserOperandException(OperandPosition.LEFT));
    }

    @Transient
    public UserSetOperand getRightOperand() {
        return operands.stream()
                .filter(operand -> operand.getPosition() == OperandPosition.RIGHT)
                .findAny()
                .orElseThrow(() -> new NullUserOperandException(OperandPosition.RIGHT));
    }
}
