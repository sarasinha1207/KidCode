package com.kidcode.core.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private final String input;
    private int position = 0; // current position in input
    private int readPosition = 0; // current reading position (after current char)
    private char ch; // current char under examination
    private int lineNumber = 1;

    private static final Map<String, TokenType> keywords = new HashMap<>();

    static {
        keywords.put("move", TokenType.MOVE);
        keywords.put("forward", TokenType.FORWARD);
        keywords.put("turn", TokenType.TURN);
        keywords.put("left", TokenType.LEFT);
        keywords.put("right", TokenType.RIGHT);
        keywords.put("say", TokenType.SAY);
        keywords.put("repeat", TokenType.REPEAT);
        keywords.put("end", TokenType.END);
        keywords.put("set", TokenType.SET);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("pen", TokenType.PEN);
        keywords.put("up", TokenType.UP);
        keywords.put("down", TokenType.DOWN);
        keywords.put("color", TokenType.COLOR);
        keywords.put("define", TokenType.DEFINE);
    }

    public Lexer(String input) {
        this.input = input;
        readChar(); // Initialize the first character
    }

    public List<Token> allTokens() {
        List<Token> tokens = new ArrayList<>();
        Token t;
        do {
            t = nextToken();
            tokens.add(t);
        } while (t.type() != TokenType.EOF);
        return tokens;
    }

    private void readChar() {
        if (readPosition >= input.length()) {
            ch = 0; // ASCII code for "NUL", signifies end of input
        } else {
            ch = input.charAt(readPosition);
        }
        position = readPosition;
        readPosition += 1;
    }

    public Token nextToken() {
        Token token;
        skipWhitespace();

        switch (ch) {
            case '=':
                if (peekChar() == '=') {
                    readChar();
                    token = new Token(TokenType.EQ, "==", lineNumber);
                } else {
                    token = new Token(TokenType.ASSIGN, String.valueOf(ch), lineNumber);
                }
                break;
            case '+': token = new Token(TokenType.PLUS, String.valueOf(ch), lineNumber); break;
            case '-': token = new Token(TokenType.MINUS, String.valueOf(ch), lineNumber); break;
            case '*': token = new Token(TokenType.STAR, String.valueOf(ch), lineNumber); break;
            case '/': token = new Token(TokenType.SLASH, String.valueOf(ch), lineNumber); break;
            case '(': token = new Token(TokenType.LPAREN, String.valueOf(ch), lineNumber); break;
            case ')': token = new Token(TokenType.RPAREN, String.valueOf(ch), lineNumber); break;
            case '[': token = new Token(TokenType.LBRACKET, String.valueOf(ch), lineNumber); break;
            case ']': token = new Token(TokenType.RBRACKET, String.valueOf(ch), lineNumber); break;
            case ',': token = new Token(TokenType.COMMA, String.valueOf(ch), lineNumber); break;
            case '"':
                token = new Token(TokenType.STRING, readString(), lineNumber);
                break;
            case '>': token = new Token(TokenType.GT, String.valueOf(ch), lineNumber); break;
            case '<': token = new Token(TokenType.LT, String.valueOf(ch), lineNumber); break;
            case '!':
                if (peekChar() == '=') {
                    readChar();
                    token = new Token(TokenType.NOT_EQ, "!=", lineNumber);
                } else {
                    token = new Token(TokenType.ILLEGAL, String.valueOf(ch), lineNumber);
                }
                break;
            case 0:
                token = new Token(TokenType.EOF, "", lineNumber);
                break;
            default:
                if (isLetter(ch)) {
                    String literal = readIdentifier();
                    TokenType type = keywords.getOrDefault(literal.toLowerCase(), TokenType.IDENTIFIER);
                    return new Token(type, literal, lineNumber);
                } else if (isDigit(ch)) {
                    return new Token(TokenType.NUMBER, readNumber(), lineNumber);
                } else {
                    token = new Token(TokenType.ILLEGAL, String.valueOf(ch), lineNumber);
                }
        }
        readChar();
        return token;
    }

    private void skipWhitespace() {
        while (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') {
            if (ch == '\n') {
                lineNumber++;
            }
            readChar();
        }
    }

    private String readIdentifier() {
        int startPosition = position;
        while (isLetter(ch)) {
            readChar();
        }
        return input.substring(startPosition, position);
    }

    private String readNumber() {
        int startPosition = position;
        while (isDigit(ch)) {
            readChar();
        }
        return input.substring(startPosition, position);
    }

    private String readString() {
        int startPosition = position + 1;
        do {
            readChar();
        } while (ch != '"' && ch != 0);
        return input.substring(startPosition, position);
    }

    private boolean isLetter(char c) {
        return 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z' || c == '_';
    }

    private boolean isDigit(char c) {
        return '0' <= c && c <= '9';
    }

    private char peekChar() {
        if (readPosition >= input.length()) {
            return 0;
        }
        return input.charAt(readPosition);
    }
} 