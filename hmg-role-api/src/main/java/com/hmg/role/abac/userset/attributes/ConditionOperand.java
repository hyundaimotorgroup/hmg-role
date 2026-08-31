package com.hmg.role.abac.userset.attributes;

import com.hmg.role.abac.common.enums.OperandSubject;
import com.hmg.role.admin.project.Project;
import com.hmg.role.util.enums.OperandDataType;
import com.hmg.role.util.enums.OperandType;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.javers.core.metamodel.annotation.ShallowReference;

@Data
@Table(name = "condition_operands")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ConditionOperand {
    public static final String PROP_OPERAND = "operand";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @ShallowReference
    private Project project;

    @Column(name = "operand")
    private String operand;

    @Enumerated(EnumType.STRING)
    @Column(name = "operand_data_type")
    private OperandDataType dataType;

    @Enumerated(EnumType.STRING)
    @Column(name = "operand_type")
    private OperandType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "operand_subject")
    private OperandSubject subject;

    @Column(name = "is_deleted")
    private boolean deleted;
}
