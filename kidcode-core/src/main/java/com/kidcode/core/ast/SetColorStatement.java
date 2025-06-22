package com.kidcode.core.ast;

public record SetColorStatement(Expression colorName) implements Statement {
    @Override
    public String tokenLiteral() { return "color"; }
} 