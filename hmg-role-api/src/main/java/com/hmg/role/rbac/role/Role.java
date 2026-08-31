package com.hmg.role.rbac.role;

import com.hmg.role.admin.project.Project;
import com.hmg.role.util.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.javers.core.metamodel.annotation.ShallowReference;

@Data
@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "roles")
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_key")
    private String key;

    public static final String PROP_KEY = "key";

    @Column(name = "description")
    private String description;

    @Column(name = "is_deleted")
    private boolean deleted;

    public static final String PROP_DELETED = "deleted";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @ShallowReference
    private Project project;

    @Column(name = "role_name")
    private String name;
}
