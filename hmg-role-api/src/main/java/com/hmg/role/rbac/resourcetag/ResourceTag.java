package com.hmg.role.rbac.resourcetag;

import com.hmg.role.rbac.resourcetype.ResourceType;
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
import lombok.NoArgsConstructor;
import org.javers.core.metamodel.annotation.ShallowReference;

@Data
@Table(name = "resource_type_tags")
@NoArgsConstructor
@Entity
public class ResourceTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "resource_type_id")
    @ShallowReference
    private ResourceType resourceType;

    @Column(name = "tag")
    private String tag;

    @Column(name = "is_delete")
    private boolean deleted;
}
