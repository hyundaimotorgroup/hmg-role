package com.hmg.role.admin.member;

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
import jakarta.persistence.Transient;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Data
@Table(name = "members")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@EqualsAndHashCode(callSuper = true)
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "member_name")
    private String name;

    @Column(name = "member_key")
    private String key;

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "is_deleted")
    private boolean deleted;

    @Column(name = "member_description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    public static final String PROP_PROJECT = "project";

    @Transient private boolean isAdmin;

    @Transient
    public void setApiKey(UUID apiKey) {
        this.apiKey = apiKey == null ? null : apiKey.toString();
    }
}
