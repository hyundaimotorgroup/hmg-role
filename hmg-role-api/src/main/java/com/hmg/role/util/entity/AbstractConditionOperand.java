package com.hmg.role.util.entity;

import com.hmg.role.abac.userset.attributes.ConditionOperand;
import com.hmg.role.util.enums.OperandDataType;
import com.hmg.role.util.enums.OperandPosition;
import com.hmg.role.util.enums.OperandType;
import jakarta.persistence.*;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@MappedSuperclass
public abstract class AbstractConditionOperand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    protected Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "position")
    private OperandPosition position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operand_id", referencedColumnName = "id")
    private ConditionOperand conditionOperand;

    @Transient
    public final String getOperand() {
        return conditionOperand.getOperand();
    }

    @Transient
    public final OperandDataType getDataType() {
        return conditionOperand.getDataType();
    }

    @Transient
    public final OperandType getType() {
        return conditionOperand.getType();
    }
}
