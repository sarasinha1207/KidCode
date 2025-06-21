package com.kidcode.core.ast;

public record SetStatement(
    Identifier name, 
    Expression value
) implements Statement {
    @Override
    public String tokenLiteral() { return "set"; }
} 