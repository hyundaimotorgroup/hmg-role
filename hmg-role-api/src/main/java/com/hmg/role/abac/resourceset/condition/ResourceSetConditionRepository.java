package com.hmg.role.abac.resourceset.condition;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceSetConditionRepository extends JpaRepository<ResourceSetCondition, Long> {}
