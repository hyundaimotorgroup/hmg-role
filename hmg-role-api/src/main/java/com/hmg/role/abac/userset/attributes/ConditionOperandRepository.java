package com.hmg.role.abac.userset.attributes;

import com.hmg.role.abac.common.enums.OperandSubject;
import com.hmg.role.abac.userset.attributes.dto.ConditionConflictDto;
import com.hmg.role.admin.project.Project;
import com.hmg.role.util.enums.OperandDataType;
import com.hmg.role.util.enums.OperandType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ConditionOperandRepository
        extends JpaRepository<ConditionOperand, Long>, JpaSpecificationExecutor<ConditionOperand> {
    Optional<ConditionOperand> findByProjectAndOperandAndTypeAndSubjectAndDeletedFalse(
            Project project, String operand, OperandType type, OperandSubject operandSubject);

    Optional<ConditionOperand> findByProjectAndOperandAndTypeAndDataTypeAndSubjectAndDeletedFalse(
            Project project,
            String operand,
            OperandType type,
            OperandDataType dataType,
            OperandSubject operandSubject);

    ConditionOperand deleteByOperandAndTypeAndSubjectAndProject(
            String operand, OperandType type, OperandSubject subject, Project project);

    void deleteByOperandAndTypeAndDataTypeAndSubjectAndProject(
            String operand,
            OperandType type,
            OperandDataType dataType,
            OperandSubject subject,
            Project project);

    @Query(
            """
                    SELECT
                        (COUNT(uco) > 0) as stillInUse,
                        FUNCTION('GROUP_CONCAT', us.key) as referrandKeysCsv
                    FROM UserSetOperand uco
                        JOIN uco.conditionOperand co
                        JOIN uco.userSetCondition uc
                        JOIN uc.userSet us
                    WHERE co.operand = :operand
                        AND co.type = :type
                        AND co.subject = 'USER_SET'
                        AND co.project = :project
                        AND co.deleted IS FALSE
                    """)
    boolean checkStillInUseInUserSet(String operand, OperandType type, Project project);

    @Query(
            """
                    SELECT (COUNT(uco) > 0)
                    FROM UserSetOperand uco
                        JOIN uco.conditionOperand co
                        JOIN uco.userSetCondition uc
                        JOIN uc.userSet us
                    WHERE co.operand = :operand
                        AND co.type = :type
                        AND co.dataType = :dataType
                        AND co.subject = 'USER_SET'
                        AND co.project = :project
                        AND co.deleted IS FALSE
                    """)
    boolean checkStillInUseInUserSet(
            String operand, OperandType type, OperandDataType dataType, Project project);

    @Query(
            """
                    SELECT COUNT(rso) > 0
                    FROM ResourceSetOperand rso
                        JOIN rso.conditionOperand co
                        JOIN rso.resourceSetCondition rc
                        JOIN rc.resourceSet rs
                    WHERE co.operand = :operand
                        AND co.type = :type
                        AND co.subject = 'RESOURCE_SET'
                        AND co.project = :project
                        AND co.deleted IS FALSE
                    """)
    boolean checkStillInUseInResourceSet(String operand, OperandType type, Project project);

    @Query(
            """
                    SELECT COUNT(rso) > 0
                    FROM ResourceSetOperand rso
                        JOIN rso.conditionOperand co
                        JOIN rso.resourceSetCondition rc
                        JOIN rc.resourceSet rs
                    WHERE co.operand = :operand
                        AND co.type = :type
                        AND co.dataType = :dataType
                        AND co.subject = 'RESOURCE_SET'
                        AND co.project = :project
                        AND co.deleted IS FALSE
                    """)
    boolean checkStillInUseInResourceSet(
            String operand, OperandType type, OperandDataType dataType, Project project);

    @Query(
            """
                    SELECT DISTINCT new com.hmg.role.abac.userset.attributes.dto.ConditionConflictDto(us.key, us.name)
                    FROM UserSetOperand uco
                        JOIN uco.conditionOperand co
                        JOIN uco.userSetCondition uc
                        JOIN uc.userSet us
                    WHERE co.operand = :operand
                        AND co.subject = 'USER_SET'
                        AND co.project = :project
                        AND co.deleted IS FALSE
                        AND us.deleted IS FALSE
                    """)
    List<ConditionConflictDto> findUserSetsUsingAttribute(String operand, Project project);

    @Query(
            """
                    SELECT DISTINCT new com.hmg.role.abac.userset.attributes.dto.ConditionConflictDto(rs.key, rs.name)
                    FROM ResourceSetOperand rco
                        JOIN rco.conditionOperand co
                        JOIN rco.resourceSetCondition rc
                        JOIN rc.resourceSet rs
                    WHERE co.operand = :operand
                        AND co.subject = 'RESOURCE_SET'
                        AND co.project = :project
                        AND co.deleted IS FALSE
                        AND rs.deleted IS FALSE
                    """)
    List<ConditionConflictDto> findResourceSetsUsingAttribute(String operand, Project project);
}
