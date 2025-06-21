package com.kidcode.core.ast;

public record SayStatement(Expression message) implements Statement {
    @Override
    public String tokenLiteral() { return "say"; }
} 