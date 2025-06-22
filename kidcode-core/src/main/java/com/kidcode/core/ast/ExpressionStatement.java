package com.kidcode.core.ast;

public record ExpressionStatement(Expression expression) implements Statement {
    @Override
    public String tokenLiteral() { return expression.tokenLiteral(); }
} 