package com.hmg.role.abac.logicalexpression.enums;

import com.hmg.role.abac.logicalexpression.interfaces.Operator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum LogicalOperator implements Operator {
    AND("&&"),
    OR("||");
    private final String symbol;
}
