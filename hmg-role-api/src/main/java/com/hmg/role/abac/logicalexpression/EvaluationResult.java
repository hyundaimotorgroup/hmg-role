package com.hmg.role.abac.logicalexpression;

public record EvaluationResult(boolean output, String reasoning) {
    public static EvaluationResult ofOutput(boolean output) {
        return new EvaluationResult(output, null);
    }

    public static EvaluationResult ofOutput(boolean output, String reasoning) {
        return new EvaluationResult(output, reasoning);
    }

    public static EvaluationResult ofError(String error) {
        return new EvaluationResult(false, error);
    }
}
