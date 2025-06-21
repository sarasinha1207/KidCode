package com.kidcode.core.parser;

import com.kidcode.core.ast.*;
import com.kidcode.core.lexer.*;
import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int position = 0;
    private final List<String> errors = new ArrayList<>();

    // Pratt parser precedence
    private enum Precedence {
        LOWEST,
        EQUALS,  // ==, !=
        LESSGREATER, // >, <
        SUM,     // + -
        PRODUCT, // * /
        PREFIX,  // -X
        INDEX    // array[index]
    }
    private static final Map<TokenType, Precedence> precedences = new HashMap<>();
    static {
        precedences.put(TokenType.EQ, Precedence.EQUALS);
        precedences.put(TokenType.NOT_EQ, Precedence.EQUALS);
        precedences.put(TokenType.LT, Precedence.LESSGREATER);
        precedences.put(TokenType.GT, Precedence.LESSGREATER);
        precedences.put(TokenType.PLUS, Precedence.SUM);
        precedences.put(TokenType.MINUS, Precedence.SUM);
        precedences.put(TokenType.STAR, Precedence.PRODUCT);
        precedences.put(TokenType.SLASH, Precedence.PRODUCT);
        precedences.put(TokenType.LBRACKET, Precedence.INDEX);
    }

    public Parser(Lexer lexer) {
        this.tokens = lexer.allTokens();
    }

    public List<String> getErrors() {
        return errors;
    }

    private Token currentToken() {
        if (position < tokens.size()) {
            return tokens.get(position);
        }
        return new Token(TokenType.EOF, "", -1); // Sentinel
    }

    private Token peekToken() {
        if (position + 1 < tokens.size()) {
            return tokens.get(position + 1);
        }
        return new Token(TokenType.EOF, "", -1);
    }

    private void nextToken() {
        position++;
    }

    public List<Statement> parseProgram() {
        List<Statement> statements = new ArrayList<>();
        while (currentToken().type() != TokenType.EOF) {
            Statement stmt = parseStatement();
            if (stmt != null) {
                statements.add(stmt);
            }
            nextToken();
        }
        return statements;
    }

    private Statement parseStatement() {
        switch (currentToken().type()) {
            case MOVE: return parseMoveStatement();
            case TURN: return parseTurnStatement();
            case SAY: return parseSayStatement();
            case REPEAT: return parseRepeatStatement();
            case SET: return parseSetStatement();
            case IF: return parseIfStatement();
            case PEN: return parsePenStatement();
            case COLOR: return parseSetColorStatement();
            case DEFINE: return parseFunctionDefinitionStatement();
            case IDENTIFIER: return parseFunctionCallStatement();
            default: return null;
        }
    }

    private MoveStatement parseMoveStatement() {
        if (peekToken().type() != TokenType.FORWARD) {
            errors.add("Error line " + currentToken().lineNumber() + ": Expected 'forward' after 'move'");
            return null;
        }
        nextToken(); // Consume 'forward'
        nextToken(); // Move to the expression
        Expression steps = parseExpression(Precedence.LOWEST);
        return new MoveStatement(steps);
    }

    private TurnStatement parseTurnStatement() {
        if (peekToken().type() != TokenType.LEFT && peekToken().type() != TokenType.RIGHT) {
            errors.add("Error line " + currentToken().lineNumber() + ": Expected 'left' or 'right' after 'turn'");
            return null;
        }
        nextToken(); // Consume 'left' or 'right'
        String direction = currentToken().literal();
        nextToken(); // Move to the expression
        Expression degrees = parseExpression(Precedence.LOWEST);
        return new TurnStatement(direction, degrees);
    }

    private SayStatement parseSayStatement() {
        if (peekToken().type() != TokenType.STRING) {
            errors.add("Error line " + currentToken().lineNumber() + ": Expected a string in quotes after 'say'");
            return null;
        }
        nextToken(); // Consume string token
        String message = currentToken().literal();
        return new SayStatement(new StringLiteral(message));
    }

    private RepeatStatement parseRepeatStatement() {
        if (peekToken().type() != TokenType.NUMBER && peekToken().type() != TokenType.IDENTIFIER) {
            errors.add("Error line " + currentToken().lineNumber() + ": Expected a number or variable after 'repeat'");
            return null;
        }
        nextToken(); // Move to the expression
        Expression times = parseExpression(Precedence.LOWEST);
        List<Statement> body = parseBlock();
        if (currentToken().type() != TokenType.END) {
            errors.add("Error line " + currentToken().lineNumber() + ": Expected 'end' to close 'repeat' block");
            return null;
        }
        if (peekToken().type() == TokenType.REPEAT) {
            nextToken(); // Consume 'repeat'
        }
        return new RepeatStatement(times, body);
    }

    private SetStatement parseSetStatement() {
        if (peekToken().type() != TokenType.IDENTIFIER) {
            errors.add("Error line " + currentToken().lineNumber() + ": Expected variable name after 'set'");
            return null;
        }
        nextToken(); // Consume IDENTIFIER
        Identifier name = new Identifier(currentToken().literal());
        if (peekToken().type() != TokenType.ASSIGN) {
            errors.add("Error line " + currentToken().lineNumber() + ": Expected '=' after variable name");
            return null;
        }
        nextToken(); // Consume =
        nextToken(); // Move to the start of the expression
        Expression value = parseExpression(Precedence.LOWEST);
        return new SetStatement(name, value);
    }

    private IfStatement parseIfStatement() {
        nextToken(); // Consume 'if'
        Expression condition = parseExpression(Precedence.LOWEST);
        List<Statement> consequence = parseBlock();
        List<Statement> alternative = null;
        if (currentToken().type() == TokenType.ELSE) {
            nextToken(); // Consume 'else'
            alternative = parseBlock();
        } else {
            if (currentToken().type() != TokenType.END) {
                errors.add("Error line " + currentToken().lineNumber() + ": Expected 'end' or 'else' to close 'if' block");
                return null;
            }
        }
        if (currentToken().type() != TokenType.END) {
            errors.add("Error line " + currentToken().lineNumber() + ": Expected 'end' to close 'if/else' block");
            return null;
        }
        if (peekToken().type() == TokenType.IF) {
            nextToken();
        }
        return new IfStatement(condition, consequence, alternative);
    }

    private List<Statement> parseBlock() {
        List<Statement> block = new ArrayList<>();
        nextToken(); // Move to the first statement in the block
        while (currentToken().type() != TokenType.END && currentToken().type() != TokenType.ELSE && currentToken().type() != TokenType.EOF) {
            Statement stmt = parseStatement();
            if (stmt != null) {
                block.add(stmt);
            }
            nextToken();
        }
        return block;
    }

    private List<Statement> parseBlock(String blockType) {
        List<Statement> block = new ArrayList<>();
        nextToken(); // Move to the first statement in the block
        while (currentToken().type() != TokenType.END && currentToken().type() != TokenType.ELSE && currentToken().type() != TokenType.EOF) {
            Statement stmt = parseStatement();
            if (stmt != null) {
                block.add(stmt);
            }
            nextToken();
        }
        return block;
    }

    private Expression parseExpression(Precedence precedence) {
        Expression left = switch (currentToken().type()) {
            case IDENTIFIER -> new Identifier(currentToken().literal());
            case NUMBER -> new IntegerLiteral(Integer.parseInt(currentToken().literal()));
            case STRING -> new StringLiteral(currentToken().literal());
            case LPAREN -> parseGroupedExpression();
            case LBRACKET -> parseListLiteral();
            default -> {
                errors.add("Error line " + currentToken().lineNumber() + ": Unexpected token " + currentToken().literal() + " in expression");
                yield null;
            }
        };
        
        while (peekToken().type() != TokenType.EOF && precedence.ordinal() < getPeekPrecedence().ordinal()) {
            TokenType peekType = peekToken().type();
            if (peekType == TokenType.LBRACKET) {
                nextToken();
                left = parseIndexExpression(left);
            } else if (isOperator(peekType)) {
                nextToken(); // Consume operator
                left = parseInfixExpression(left);
            } else {
                return left;
            }
        }
        return left;
    }

    private boolean isOperator(TokenType type) {
        return type == TokenType.PLUS || type == TokenType.MINUS || type == TokenType.STAR ||
               type == TokenType.SLASH || type == TokenType.EQ || type == TokenType.NOT_EQ ||
               type == TokenType.LT || type == TokenType.GT;
    }

    private Precedence getPeekPrecedence() {
        return precedences.getOrDefault(peekToken().type(), Precedence.LOWEST);
    }

    private Expression parseInfixExpression(Expression left) {
        String operator = currentToken().literal();
        Precedence p = precedences.get(currentToken().type());
        nextToken();
        Expression right = parseExpression(p);
        return new InfixExpression(left, operator, right);
    }

    private Expression parseGroupedExpression() {
        nextToken(); // Consume (
        Expression exp = parseExpression(Precedence.LOWEST);
        if (peekToken().type() != TokenType.RPAREN) {
            errors.add("Error line " + currentToken().lineNumber() + ": Expected ')' to close expression");
            return null;
        }
        nextToken(); // Consume )
        return exp;
    }

    private PenStatement parsePenStatement() {
        if (peekToken().type() != TokenType.UP && peekToken().type() != TokenType.DOWN) {
            errors.add("Error line " + currentToken().lineNumber() + ": Expected 'up' or 'down' after 'pen'");
            return null;
        }
        nextToken(); // Consume 'up' or 'down'
        String state = currentToken().literal();
        return new PenStatement(state);
    }

    private SetColorStatement parseSetColorStatement() {
        if (peekToken().type() != TokenType.STRING && peekToken().type() != TokenType.IDENTIFIER) {
            errors.add("Error line " + currentToken().lineNumber() + ": Expected a color name or variable after 'color'");
            return null;
        }
        nextToken(); // Move to the color expression
        Expression colorName = parseExpression(Precedence.LOWEST);
        return new SetColorStatement(colorName);
    }

    private FunctionDefinitionStatement parseFunctionDefinitionStatement() {
        if (peekToken().type() != TokenType.IDENTIFIER) {
            errors.add("Error line " + currentToken().lineNumber() + ": Expected function name after 'define'");
            return null;
        }
        nextToken(); // Consume function name
        Identifier name = new Identifier(currentToken().literal());

        List<Identifier> parameters = new ArrayList<>();
        // Parse parameters until the line breaks or block starts
        while (peekToken().type() == TokenType.IDENTIFIER) {
            nextToken();
            parameters.add(new Identifier(currentToken().literal()));
        }

        List<Statement> body = parseBlock("define");
        if (currentToken().type() != TokenType.END) {
            errors.add("Error line " + currentToken().lineNumber() + ": Expected 'end define' to close function definition");
            return null;
        }
        if (peekToken().type() == TokenType.DEFINE) {
            nextToken(); // Consume 'define'
        }
        return new FunctionDefinitionStatement(name, parameters, body);
    }

    private FunctionCallStatement parseFunctionCallStatement() {
        Identifier function = new Identifier(currentToken().literal());
        List<Expression> arguments = new ArrayList<>();
        
        // Arguments can be numbers, variables, strings, or parenthesized expressions
        while (peekToken().type() == TokenType.NUMBER ||
               peekToken().type() == TokenType.IDENTIFIER ||
               peekToken().type() == TokenType.STRING ||
               peekToken().type() == TokenType.LPAREN) {
            nextToken();
            arguments.add(parseExpression(Precedence.LOWEST));
        }

        return new FunctionCallStatement(function, arguments);
    }

    private Expression parseListLiteral() {
        List<Expression> elements = new ArrayList<>();
        if (peekToken().type() == TokenType.RBRACKET) { // Handle empty list []
            nextToken();
            return new ListLiteral(elements);
        }
        nextToken();
        elements.add(parseExpression(Precedence.LOWEST));
        while (peekToken().type() == TokenType.COMMA) {
            nextToken();
            nextToken();
            elements.add(parseExpression(Precedence.LOWEST));
        }
        if (peekToken().type() != TokenType.RBRACKET) {
            errors.add("Error line " + currentToken().lineNumber() + ": Expected ']' to close list");
            return null;
        }
        nextToken();
        return new ListLiteral(elements);
    }

    private Expression parseIndexExpression(Expression left) {
        nextToken(); // Consume '['
        Expression index = parseExpression(Precedence.LOWEST);
        if (peekToken().type() != TokenType.RBRACKET) {
            errors.add("Error line " + currentToken().lineNumber() + ": Expected ']' to close index expression");
            return null;
        }
        nextToken(); // Consume ']'
        return new IndexExpression(left, index);
    }
} 