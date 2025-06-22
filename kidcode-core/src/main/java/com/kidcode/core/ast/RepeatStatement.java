package com.kidcode.core.ast;

import java.util.List;

public record RepeatStatement(
    Expression times, 
    List<Statement> body
) implements Statement {
    @Override
    public String tokenLiteral() { return "repeat"; }
} 