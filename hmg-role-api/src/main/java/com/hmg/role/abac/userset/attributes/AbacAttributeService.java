package com.hmg.role.abac.userset.attributes;

import com.hmg.role.abac.common.enums.OperandSubject;
import com.hmg.role.abac.userset.attributes.dto.ConditionAttributeDeleteDto;
import com.hmg.role.abac.userset.attributes.dto.ConditionAttributeDto;
import com.hmg.role.abac.userset.attributes.dto.ConditionAttributeSearchDto;
import com.hmg.role.admin.project.Project;
import com.hmg.role.util.dto.ListResponseDto;
import com.hmg.role.util.enums.OperandDataType;
import com.hmg.role.util.enums.OperandType;
import java.util.Collection;

public interface AbacAttributeService {
    ListResponseDto<ConditionAttributeDto> getAll(
            ConditionAttributeSearchDto params, OperandSubject subject);

    ConditionAttributeDto create(ConditionAttributeDto operandDto, OperandSubject subject);

    void delete(ConditionAttributeDeleteDto operandDto, OperandSubject subject);

    ConditionOperand getOrCreateOperand(
            OperandSubject operandSubject,
            String operand,
            OperandType type,
            OperandDataType dataType,
            Project project);

    void opportunisticDelete(
            String operand, OperandType type, OperandSubject subject, Project project);

    void opportunisticDeleteLiterals(
            Collection<ConditionOperand> conditionOperands,
            OperandSubject subject,
            Project project);
}
