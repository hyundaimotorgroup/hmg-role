package com.hmg.role.rbac.resourcetype;

import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.resourcetype.projections.ResourceTypeParentProjection;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceTypeRepository
        extends JpaRepository<ResourceType, Long>, JpaSpecificationExecutor<ResourceType> {

    List<ResourceType> findByKeyInAndProjectAndDeletedFalse(List<String> key, Project project);

    Optional<ResourceType> findByKeyAndProjectAndDeletedFalse(String key, Project project);

    List<ResourceType> findByParentInAndProjectAndDeletedFalse(
            List<ResourceType> parents, Project project);

    List<ResourceType> findAllByKeyInAndProjectAndDeletedFalse(
            Collection<String> keys, Project project);

    @Query(
            """
                        SELECT count(r) as pageCount FROM ResourceType r
                        WHERE r.project = :project
                        AND r.deleted = false
                    """)
    int countResourceTypeByProjectAndDeletedFalse(Project project);

    @Query(
            """
                        SELECT count(r) as pageCount FROM ResourceType r
                        WHERE r.project = :project
                        AND EXISTS (
                            SELECT 1 FROM ResourceAction ra
                            WHERE ra.resourceType = r
                            AND lower(ra.actionName) LIKE lower(CONCAT('%',:andActionLike,'%')) ESCAPE '!'
                        )
                        AND r.deleted = false
                    """)
    int countResourceTypeByProjectAndActionLikeAndDeletedFalse(
            Project project, String andActionLike);

    @Query(
            """
                        SELECT count(r) as pageCount FROM ResourceType r
                        WHERE r.project = :project
                        AND lower(r.name) LIKE lower(CONCAT('%',:name,'%')) ESCAPE '!'
                        AND r.deleted = false
                        AND r.parent IS NULL
                    """)
    int countResourceTypeByProjectAndNameAndDeletedFalse(Project project, String name);

    @Query(
            """
                        SELECT count(r) as pageCount FROM ResourceType r
                        WHERE r.project = :project
                        AND lower(r.key) LIKE lower(CONCAT('%',:key,'%')) ESCAPE '!'
                        AND r.deleted = false
                        AND r.parent IS NULL
                    """)
    int countResourceTypeByProjectAndKeyAndDeletedFalse(Project project, String key);

    @Query(
            """
                        SELECT count(DISTINCT r) as pageCount FROM ResourceType r
                        JOIN r.resourceTags rtag
                        WHERE r.project = :project
                        AND lower(rtag.tag) LIKE lower(CONCAT('%',:tag,'%')) ESCAPE '!'
                        AND r.deleted = false
                        AND r.parent IS NULL
                    """)
    int countResourceTypeByProjectAndTagAndDeletedFalse(Project project, String tag);

    @Query(
            """
                        SELECT count(DISTINCT r) as pageCount FROM ResourceType r
                        LEFT JOIN r.resourceTags rtag
                        WHERE r.project = :project
                        AND (
                            (lower(r.name) LIKE lower(CONCAT('%',:keyword,'%')) ESCAPE '!'
                            OR lower(rtag.tag) LIKE lower(CONCAT('%',:keyword,'%')) ESCAPE '!')
                        ) AND r.deleted = false
                        AND r.parent IS NULL
                    """)
    int countResourceTypeByProjectAndNameOrTagAndDeletedFalse(Project project, String keyword);

    @Query(
            """
                        SELECT count(r) as pageCount FROM ResourceType r
                        JOIN r.resourceActions ra
                        WHERE r.project = :project
                        AND lower(ra.actionName) LIKE lower(CONCAT('%',:action,'%')) ESCAPE '!'
                        AND r.deleted = false
                        AND r.parent IS NULL
                    """)
    int countResourceTypeByProjectAndActionAndDeletedFalse(Project project, String action);

    @Query(
            """
                        SELECT count(r) as pageCount FROM ResourceType r
                        WHERE r.project = :project
                        AND lower(r.name) LIKE lower(CONCAT('%',:name,'%')) ESCAPE '!'
                        AND EXISTS (
                            SELECT 1 FROM ResourceAction ra
                            WHERE ra.resourceType = r
                            AND lower(ra.actionName) LIKE lower(CONCAT('%',:andActionLike,'%')) ESCAPE '!'
                        )
                        AND r.deleted = false
                        AND r.parent IS NULL
                    """)
    int countResourceTypeByProjectAndNameAndActionLikeAndDeletedFalse(
            Project project, String name, String andActionLike);

    @Query(
            """
                        SELECT count(r) as pageCount FROM ResourceType r
                        WHERE r.project = :project
                        AND lower(r.key) LIKE lower(CONCAT('%',:key,'%')) ESCAPE '!'
                        AND EXISTS (
                            SELECT 1 FROM ResourceAction ra
                            WHERE ra.resourceType = r
                            AND lower(ra.actionName) LIKE lower(CONCAT('%',:andActionLike,'%')) ESCAPE '!'
                        )
                        AND r.deleted = false
                        AND r.parent IS NULL
                    """)
    int countResourceTypeByProjectAndKeyAndActionLikeAndDeletedFalse(
            Project project, String key, String andActionLike);

    @Query(
            """
                        SELECT count(DISTINCT r) as pageCount FROM ResourceType r
                        JOIN r.resourceTags rtag
                        WHERE r.project = :project
                        AND lower(rtag.tag) LIKE lower(CONCAT('%',:tag,'%')) ESCAPE '!'
                        AND EXISTS (
                            SELECT 1 FROM ResourceAction ra
                            WHERE ra.resourceType = r
                            AND lower(ra.actionName) LIKE lower(CONCAT('%',:andActionLike,'%')) ESCAPE '!'
                        )
                        AND r.deleted = false
                        AND r.parent IS NULL
                    """)
    int countResourceTypeByProjectAndTagAndActionLikeAndDeletedFalse(
            Project project, String tag, String andActionLike);

    @Query(
            """
                        SELECT count(DISTINCT r) as pageCount FROM ResourceType r
                        LEFT JOIN r.resourceTags rtag
                        WHERE r.project = :project
                        AND (
                            (lower(r.name) LIKE lower(CONCAT('%',:keyword,'%')) ESCAPE '!'
                            OR lower(rtag.tag) LIKE lower(CONCAT('%',:keyword,'%')) ESCAPE '!')
                        ) AND EXISTS (
                            SELECT 1 FROM ResourceAction ra
                            WHERE ra.resourceType = r
                            AND lower(ra.actionName) LIKE lower(CONCAT('%',:andActionLike,'%')) ESCAPE '!'
                        )
                        AND r.deleted = false
                        AND r.parent IS NULL
                    """)
    int countResourceTypeByProjectAndNameOrTagAndActionLikeAndDeletedFalse(
            Project project, String keyword, String andActionLike);

    @Query(
            """
                    SELECT
                        rt.name AS name,
                        rt.key AS key
                    FROM ResourceType rt
                    WHERE rt.project = :project
                      AND rt.deleted = false
                      AND (:currentResourceTypeKey IS NULL
                           OR :currentResourceTypeKey = ''
                           OR rt.key <> :currentResourceTypeKey)
                    """)
    List<ResourceTypeParentProjection> findOtherThan(
            Project project, String currentResourceTypeKey);

    Project project(Project project);

    List<ResourceType> findByKeyInAndProjectAndDeletedFalse(
            Collection<String> parentKeys, Project project);

    @Query(
            """
                            SELECT DISTINCT
                                ra.actionName AS name
                            FROM ResourceAction ra
                            JOIN ra.resourceType rt
                            WHERE ra.deleted = false
                            AND rt.project = :project
                    """)
    List<String> findAllResourceActionNames(Project project);

    @Query(
            """
                            SELECT DISTINCT
                                rtag.tag AS tag
                            FROM ResourceTag rtag
                            JOIN rtag.resourceType rt
                            WHERE rtag.deleted = false
                            AND rt.project = :project
                    """)
    List<String> findAllResourceTagNames(Project project);
}
