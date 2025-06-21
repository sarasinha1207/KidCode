package com.kidcode.core.ast;

public record IntegerLiteral(int value) implements Expression {
    @Override
    public String tokenLiteral() { return String.valueOf(value); }
} 