package com.hmg.role.admin.project;

import com.hmg.role.abac.scope.AbacScope;
import com.hmg.role.rbac.scope.Scope;
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
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Data
@Table(name = "projects")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@EqualsAndHashCode(
        callSuper = true,
        exclude = {"defaultScopeRbac", "defaultScopeAbac"})
public class Project extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long id;

    @Column(name = "project_key")
    private String key;

    @Column(name = "project_name")
    private String name;

    @Column(name = "project_description")
    private String description;

    @Column(name = "is_deleted")
    private boolean deleted;

    @Column(name = "company")
    private String company; // TODO move to config table

    @Column(name = "operating_country")
    private String operatingCountry; // TODO move to config table

    @Column(name = "personal_data_self_handled")
    private boolean personalDataSelfHandled; // TODO move to config table

    @Column(name = "service_consent_history_url")
    private String serviceConsentHistoryUrl; // TODO move to config table

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_scope_id_rbac")
    private Scope defaultScopeRbac;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_scope_id_abac")
    private AbacScope defaultScopeAbac;

    @Transient
    public String getHmgAdminModuleCode() {
        return key;
    }
}
