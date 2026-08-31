package com.hmg.role.abac.logicalexpression.enums;

import com.hmg.role.abac.logicalexpression.interfaces.Operator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum RelationalOperator implements Operator {
    EQ("=="),
    NQ("!="),
    LE("<="),
    GE(">="),
    GT(">"),
    LT("<");
    private final String symbol;
}
