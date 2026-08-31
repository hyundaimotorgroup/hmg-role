package com.hmg.role.abac.resourceset.action;

import com.hmg.role.abac.resourceset.ResourceSet;
import com.hmg.role.admin.project.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceSetActionRepository extends JpaRepository<ResourceSetAction, Long> {

    List<ResourceSetAction> findByResourceSetAndDeletedFalse(ResourceSet resourceSet);

    @Query(
            """
            SELECT DISTINCT rsa.actionName
            FROM ResourceSetAction rsa
            JOIN rsa.resourceSet rs
            WHERE rsa.deleted = false
                AND rs.project = :project
            """)
    List<String> findAllDistinctActionNames(Project project);
}
