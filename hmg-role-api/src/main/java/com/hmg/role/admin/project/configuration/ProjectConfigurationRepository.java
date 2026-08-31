package com.hmg.role.admin.project.configuration;

import com.hmg.role.admin.project.Project;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectConfigurationRepository
        extends JpaRepository<ProjectConfiguration, Integer> {
    ProjectConfiguration findById(int id);

    @Query(
            value =
                    """
                    FROM ProjectConfiguration pc
                        JOIN pc.project p
                    WHERE p.deleted IS false
                        AND p = :project
                        AND pc.configurationKey = :configurationKey
                    """)
    ProjectConfiguration findByProjectAndConfigurationKey(Project project, String configurationKey);

    @Query(
            value =
                    """
                    FROM ProjectConfiguration pc
                        JOIN pc.project p
                    WHERE p.deleted IS false
                        AND p.key = :projectKey
                        AND pc.configurationKey = :configurationKey
                    """)
    ProjectConfiguration findByProjectKeyAndConfigurationKey(
            String projectKey, String configurationKey);

    @Query(
            value =
                    """
                    SELECT count(pc) > 0
                    FROM ProjectConfiguration pc
                        JOIN pc.project p
                    WHERE p.deleted IS false
                        AND p = :project
                        AND pc.configurationKey = :configurationKey
                    """)
    boolean existsByProjectAndMemberAndConfigurationKey(Project project, String configurationKey);

    @Query(
            """
            FROM ProjectConfiguration pc
            JOIN FETCH pc.project p
            WHERE pc.configurationKey = :configurationKey
                AND pc.expiryUtc < :cutOff
            """)
    List<ProjectConfiguration> getExpiredSecurityCredentials(
            String configurationKey, LocalDateTime cutOff);
}
