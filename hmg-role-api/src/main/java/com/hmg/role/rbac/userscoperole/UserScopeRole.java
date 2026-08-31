package com.hmg.role.rbac.userscoperole;

import com.hmg.role.rbac.role.Role;
import com.hmg.role.rbac.scope.Scope;
import com.hmg.role.rbac.user.User;
import jakarta.persistence.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import org.javers.core.metamodel.annotation.ShallowReference;

@Data
@Entity
@Table(name = "user_scope_roles")
public class UserScopeRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @ShallowReference
    private User user;

    public static final String PROP_USER = "user";

    @ManyToOne
    @JoinColumn(name = "role_id")
    @ShallowReference
    private Role role;

    public static final String PROP_ROLE = "role";

    @ManyToOne
    @JoinColumn(name = "scope_id")
    @ShallowReference
    private Scope scope;

    public static final String PROP_SCOPE = "scope";

    @Column(name = "is_deleted")
    private boolean deleted;

    public static final String PROP_DELETED = "deleted";
}
