package com.hmg.role.rbac.scope;

import com.hmg.role.admin.project.Project;
import com.hmg.role.util.entity.BaseEntity;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.javers.core.metamodel.annotation.ShallowReference;

@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
@Data
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EqualsAndHashCode(callSuper = true, exclude = "project")
@Table(name = "scopes")
@ToString(exclude = "project")
public class Scope extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scope_id")
    private Long scopeId; // TODO: rename the field and the column to id

    @NonNull
    @Column(name = "scope_key")
    private String key;

    @Column(name = "scope_name")
    private String name;

    public static final String PROP_SCOPE_KEY = "key";

    public static final String PROP_NAME = "name";

    @Column(name = "is_deleted")
    private boolean deleted;

    public static final String PROP_DELETED = "deleted";

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @ShallowReference
    private Project project;

    public static final String PROP_PROJECT = "project";

    public Scope(String key, String name, Project project) {
        this.key = key;
        this.name = name;
        this.project = project;
    }
}
