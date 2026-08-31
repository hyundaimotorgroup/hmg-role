package com.hmg.role.abac.userset.attributes;

import com.hmg.role.abac.common.enums.OperandSubject;
import com.hmg.role.abac.userset.attributes.dto.ConditionAttributeDto;
import com.hmg.role.admin.project.Project;
import com.hmg.role.common.config.CommonMapperConfig;
import com.hmg.role.util.enums.OperandDataType;
import com.hmg.role.util.enums.OperandType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public abstract class ConditionOperandMapper {
    @Mapping(target = "key", source = "entity.operand")
    public abstract ConditionAttributeDto toDto(ConditionOperand entity);

    @Mapping(target = "operand", source = "dto.key")
    public abstract ConditionOperand toEntity(ConditionAttributeDto dto);

    public ConditionOperand toConditionOperand(
            OperandSubject subject,
            String operand,
            OperandType type,
            OperandDataType dataType,
            Project project) {
        ConditionOperand conditionOperandMapped = new ConditionOperand();
        conditionOperandMapped.setOperand(operand);
        conditionOperandMapped.setType(type);
        conditionOperandMapped.setDataType(dataType);
        conditionOperandMapped.setProject(project);
        conditionOperandMapped.setSubject(subject);
        return conditionOperandMapped;
    }
}
