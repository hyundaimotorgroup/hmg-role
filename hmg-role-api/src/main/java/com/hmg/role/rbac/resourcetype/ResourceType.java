package com.hmg.role.rbac.resourcetype;

import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.resourceaction.ResourceAction;
import com.hmg.role.rbac.resourcetag.ResourceTag;
import com.hmg.role.util.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.javers.core.metamodel.annotation.ShallowReference;

@Data
@Entity
@Cacheable
@Table(name = "resource_types")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ResourceType extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_type_id")
    private Long id;

    @Column(name = "resource_type_description")
    private String description;

    @Column(name = "resource_type_key")
    private String key;

    @Column(name = "is_deleted")
    private boolean deleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @ShallowReference
    private Project project;

    @Column(name = "resource_type_name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @ShallowReference
    private ResourceType parent;

    @OneToMany(mappedBy = "resourceType", fetch = FetchType.LAZY)
    @DiffIgnore
    private List<ResourceAction> resourceActions;

    @OneToMany(mappedBy = "resourceType")
    @DiffIgnore
    private List<ResourceTag> resourceTags;

    @Deprecated
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @DiffIgnore
    private List<ResourceType> children = new ArrayList<>();
}
