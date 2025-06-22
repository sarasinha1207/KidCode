package com.kidcode.core.ast;

public record MoveStatement(Expression steps) implements Statement {
    @Override
    public String tokenLiteral() { return "move"; }
} 