package com.hmg.role.abac.userset.condition;

import com.hmg.role.abac.userset.UserSet;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSetConditionRepository extends JpaRepository<UserSetCondition, Long> {

    List<UserSetCondition> findByUserSet(UserSet userSet);
}
