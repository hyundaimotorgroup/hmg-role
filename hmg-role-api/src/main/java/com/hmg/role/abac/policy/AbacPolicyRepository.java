package com.hmg.role.abac.policy;

import com.hmg.role.admin.project.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AbacPolicyRepository extends JpaRepository<AbacPolicy, Long> {

    Page<AbacPolicy> findByProjectAndDeletedFalse(Project project, Pageable pageable);

    List<AbacPolicy> findByKeyInAndProjectAndDeletedFalse(List<String> keys, Project project);

    Optional<AbacPolicy> findByKeyAndProjectAndDeletedFalse(String key, Project project);

    // not doing SELECT for the GET method here due to hibernate's dumb interpretation
    // of pagination that causes it to fetch the ENTIRE table
    // the query is in the policy item repository
}
