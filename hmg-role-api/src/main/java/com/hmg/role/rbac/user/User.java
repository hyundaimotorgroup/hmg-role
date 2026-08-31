package com.hmg.role.rbac.user;

import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.role.Role;
import com.hmg.role.rbac.userscoperole.UserScopeRole;
import com.hmg.role.util.entity.BaseEntity;
import com.hmg.role.util.sqlconverter.MapStringToJsonTextConverter;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.javers.core.metamodel.annotation.ShallowReference;

@Data
@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Deprecated(since = "ScopedRole entity is ready to use", forRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_scope_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @DiffIgnore
    private List<Role> roles;

    // TODO make sure filter the UserScopeRole soft deleted
    @Deprecated
    @DiffIgnore
    @OneToMany(mappedBy = "user")
    private List<UserScopeRole> scopedRoles; // TODO rename to scopeRoles for naming consistency

    public static final String PROP_SCOPEDROLES = "scopedRoles";

    @Column(name = "user_key")
    private String userKey;

    public static final String PROP_USERKEY = "userKey";

    @Column(name = "user_name")
    private String name;

    public static final String PROP_NAME = "name";

    @Column(name = "is_deleted")
    private boolean deleted;

    @Column(name = "user_metadata_json")
    @Convert(converter = MapStringToJsonTextConverter.class)
    private Map<String, String> metadata;

    @ManyToOne
    @JoinColumn(name = "project_id")
    @ShallowReference
    private Project project;

    public static final String PROP_PROJECT = "project";
}
