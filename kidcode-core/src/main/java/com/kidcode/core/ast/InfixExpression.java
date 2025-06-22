package com.kidcode.core.ast;

// Represents an expression like '5 + 5' or 'x * 2'
public record InfixExpression(
    Expression left, 
    String operator, 
    Expression right
) implements Expression {
    @Override
    public String tokenLiteral() { return "(" + left.tokenLiteral() + " " + operator + " " + right.tokenLiteral() + ")"; }
} 