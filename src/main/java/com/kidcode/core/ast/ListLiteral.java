package com.kidcode.core.ast;

import java.util.List;
 
public record ListLiteral(List<Expression> elements) implements Expression {
    @Override
    public String tokenLiteral() { return "["; }
} 