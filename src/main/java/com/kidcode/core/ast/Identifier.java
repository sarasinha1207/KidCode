package com.kidcode.core.ast;

// Represents a variable name, e.g., 'x' in 'set x = 10'
public record Identifier(String value) implements Expression {
    @Override
    public String tokenLiteral() { return value; }
} 