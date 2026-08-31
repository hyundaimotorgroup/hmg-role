package com.hmg.role.rbac.resourceaction;

import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.resourcetype.ResourceType;
import jakarta.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceActionRepository extends JpaRepository<ResourceAction, Long> {

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    Stream<ResourceAction>
            findAllByResourceTypeKey_AndActionNameInAndResourceTypeProjectAndDeletedIsFalse(
                    String resourceTypeKey, Collection<String> actionNames, Project project);

    List<ResourceAction> findAllByResourceTypeAndDeletedIsFalse(ResourceType resourceType);

    List<ResourceAction> findAllByResourceTypeInAndDeletedIsFalse(List<ResourceType> resourceTypes);

    Stream<ResourceAction> findAllByResourceTypeKeyAndResourceTypeProjectAndDeletedIsFalse(
            String resourceTypeKey, Project project);

    @Query(
            """
                from ResourceAction ra
                join fetch ra.resourceType rt
                where rt.project = :project
                and rt.key in (:resourceTypeKey)
                and ra.deleted = false
                and rt.deleted = false
            """)
    List<ResourceAction> findAllByResourceTypeKeyIn(
            Collection<String> resourceTypeKey, Project project);
}
