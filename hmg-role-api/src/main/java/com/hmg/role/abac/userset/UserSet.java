package com.hmg.role.abac.userset;

import com.hmg.role.abac.userset.condition.UserSetCondition;
import com.hmg.role.util.entity.AbstractConditionGroup;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Data
@Table(name = "user_sets")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
public class UserSet extends AbstractConditionGroup {
    public static final String PROP_KEY = "key";
    public static final String PROP_NAME = "name";

    @Column(name = "user_set_key")
    private String key;

    @Column(name = "user_set_name", nullable = false)
    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "userSet")
    private List<UserSetCondition> conditions;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_set_parents",
            joinColumns = @JoinColumn(name = "user_set_id"),
            inverseJoinColumns = @JoinColumn(name = "parent_id"))
    private List<UserSet> parents;
}
