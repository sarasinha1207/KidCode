package com.kidcode.core.ast;

import java.util.List;

public record FunctionCallStatement(
    Identifier function,
    List<Expression> arguments
) implements Statement {
    @Override
    public String tokenLiteral() { return function.tokenLiteral(); }
} 