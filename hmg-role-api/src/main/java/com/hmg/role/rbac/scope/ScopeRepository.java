package com.hmg.role.rbac.scope;

import com.hmg.role.admin.project.Project;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ScopeRepository
        extends JpaRepository<Scope, Long>,
                JpaSpecificationExecutor<
                        Scope> { // TODO broken start due to AccessControlModel fields

    Optional<Scope> findByKeyAndProjectAndDeletedIsFalse(String key, Project project);

    Page<Scope> findByProjectAndDeletedIsFalseOrderByNameAsc(Project project, Pageable pageable);

    boolean existsByKeyAndProjectAndDeletedFalse(String key, Project project);

    boolean existsByNameAndProjectAndDeletedFalse(String name, Project project);

    void deleteByKeyAndProjectAndDeletedFalse(String key, Project project);

    boolean existsByKeyInAndProjectAndDeletedFalse(List<String> key, Project project);

    List<Scope> findByKeyInAndProjectAndDeletedFalse(Collection<String> key, Project project);

    List<Scope> findByProjectInAndDeletedFalseOrderByCreatedAtAsc(List<Project> projects);

    List<Scope> findScopeByKeyInAndProjectIdAndDeletedFalse(List<String> key, Long projectId);

    @Query("SELECT s FROM Scope s JOIN FETCH s.project WHERE s.deleted = false")
    List<Scope> findAllActive();

    List<Scope> findByKeyInAndProjectAndDeletedFalse(List<String> keys, Project project);

    Optional<Scope> findByKeyAndProjectAndDeletedFalse(String key, Project project);
}
