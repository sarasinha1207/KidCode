package com.kidcode.core.ast;

public record TurnStatement(String direction, Expression degrees) implements Statement {
    @Override
    public String tokenLiteral() { return "turn"; }
} 