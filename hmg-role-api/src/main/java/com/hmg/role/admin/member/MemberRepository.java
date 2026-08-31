package com.hmg.role.admin.member;

import com.hmg.role.admin.project.Project;
import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByApiKeyAndDeletedFalse(String apiKey);

    List<Member> findByApiKeyInAndDeletedFalse(List<String> apiKeys);

    Optional<Member> findByKeyAndProjectAndDeletedFalse(String key, Project project);

    List<Member> findByKeyInAndProjectAndDeletedFalse(List<String> key, Project project);

    Page<Member> findByProjectAndDeletedFalseOrderByUpdatedAtDesc(
            Project project, Pageable pageable);

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query(
            """
                    from Member m
                    join fetch m.project p
                    where m.deleted = false
                    and p.deleted = false
                    and m.apiKey = :apiKey
                    """)
    Optional<Member> findWithProjectByApiKey(String apiKey);
}
