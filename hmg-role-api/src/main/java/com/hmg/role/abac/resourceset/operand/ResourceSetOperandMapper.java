package com.hmg.role.abac.resourceset.operand;

import com.hmg.role.abac.logicalexpression.dto.ConditionDto;
import com.hmg.role.abac.logicalexpression.dto.OperandDto;
import com.hmg.role.abac.resourceset.ResourceSetMapper;
import com.hmg.role.abac.resourceset.condition.ResourceSetCondition;
import com.hmg.role.abac.userset.attributes.ConditionOperand;
import com.hmg.role.util.enums.OperandPosition;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// @Mapper(config = CommonMapperConfig.class) // disabled since this is a concrete mapping class, no
// mapper needed
@Service
@RequiredArgsConstructor
public class ResourceSetOperandMapper {

    protected final ResourceSetMapper resourceSetMapper;

    public ResourceSetOperand toResourceSetOperand(
            OperandPosition operandPosition,
            ConditionOperand condition,
            ResourceSetCondition resourceCondition) {
        var operand = new ResourceSetOperand();
        operand.setConditionOperand(condition);
        operand.setPosition(operandPosition);
        operand.setResourceSetCondition(resourceCondition);
        return operand;
    }

    public List<ConditionDto> toResourceConditionList(List<ResourceSetCondition> conditions) {
        List<ConditionDto> conditionDtos = new ArrayList<>();
        for (var condition : conditions) {
            OperandDto left = null;
            OperandDto right = null;
            for (var resourceOperand : condition.getOperands()) {
                if (resourceOperand.getPosition() == OperandPosition.LEFT) {
                    left = resourceSetMapper.toOperandDto(resourceOperand);
                } else if (resourceOperand.getPosition() == OperandPosition.RIGHT) {
                    right = resourceSetMapper.toOperandDto(resourceOperand);
                }
            }
            var conditionDto =
                    resourceSetMapper.toConditionDto(left, condition.getOperator(), right);
            conditionDtos.add(conditionDto);
        }
        return conditionDtos;
    }
}
