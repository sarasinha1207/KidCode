package com.kidcode.core.ast;

public record PenStatement(String state) implements Statement {
    @Override
    public String tokenLiteral() { return "pen"; }
} 