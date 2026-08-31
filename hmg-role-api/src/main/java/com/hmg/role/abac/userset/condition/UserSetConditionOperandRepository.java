package com.hmg.role.abac.userset.condition;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSetConditionOperandRepository extends JpaRepository<UserSetOperand, Long> {

    List<UserSetOperand> findByUserSetCondition(UserSetCondition userSetCondition);
}
