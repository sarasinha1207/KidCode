package com.kidcode.core.lexer;

// Using a record for a simple, immutable data carrier
public record Token(TokenType type, String literal, int lineNumber) {} 