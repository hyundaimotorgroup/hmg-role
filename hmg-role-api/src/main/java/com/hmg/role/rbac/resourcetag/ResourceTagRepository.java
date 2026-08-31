package com.hmg.role.rbac.resourcetag;

import com.hmg.role.rbac.resourcetype.ResourceType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceTagRepository extends JpaRepository<ResourceTag, Long> {

    List<ResourceTag> findAllByResourceTypeInAndDeletedFalse(List<ResourceType> resourceTypeList);

    List<ResourceTag> findByResourceTypeAndDeletedFalse(ResourceType resourceType);
}
