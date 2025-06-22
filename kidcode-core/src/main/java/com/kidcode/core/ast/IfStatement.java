package com.kidcode.core.ast;

import java.util.List;

public record IfStatement(
    Expression condition,
    List<Statement> consequence,
    List<Statement> alternative // This can be null if there is no 'else' block
) implements Statement {
    @Override
    public String tokenLiteral() { return "if"; }
} 