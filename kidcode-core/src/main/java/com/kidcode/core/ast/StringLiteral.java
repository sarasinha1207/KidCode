package com.kidcode.core.ast;

public record StringLiteral(String value) implements Expression {
    @Override
    public String tokenLiteral() { return value; }
} 