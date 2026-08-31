package com.hmg.role.util.entity;

import com.hmg.role.admin.project.Project;
import com.hmg.role.util.enums.ConditionGroupOperator;
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
import lombok.Data;
import org.javers.core.metamodel.annotation.ShallowReference;

@Data
@MappedSuperclass
public abstract class AbstractConditionGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(name = "description")
    protected String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_group_operator")
    protected ConditionGroupOperator conditionGroupOperator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @ShallowReference
    protected Project project;

    @Column(name = "is_deleted", nullable = false)
    protected boolean deleted = false;
}
