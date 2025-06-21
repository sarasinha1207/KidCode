package com.kidcode.core.ast;
 
public record IndexExpression(Expression left, Expression index) implements Expression {
    @Override
    public String tokenLiteral() { return "["; }
} 