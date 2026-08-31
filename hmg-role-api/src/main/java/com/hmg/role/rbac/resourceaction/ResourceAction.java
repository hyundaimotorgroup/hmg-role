package com.hmg.role.rbac.resourceaction;

import com.hmg.role.rbac.resourcetype.ResourceType;
import jakarta.persistence.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.javers.core.metamodel.annotation.ShallowReference;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "resource_actions")
@Entity
public class ResourceAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_action_id")
    private Long id;

    @ToString.Include
    @Column(name = "action_name")
    private String actionName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_type_id")
    @ShallowReference
    private ResourceType resourceType;

    @ToString.Include
    @Column(name = "is_deleted")
    private boolean deleted;
}
