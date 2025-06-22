package com.kidcode.core.ast;

import java.util.List;

public record FunctionDefinitionStatement(
    Identifier name,
    List<Identifier> parameters,
    List<Statement> body
) implements Statement {
    @Override
    public String tokenLiteral() { return "define"; }
} 