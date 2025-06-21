package com.kidcode.core.evaluator;

import com.kidcode.core.ast.*;
import java.awt.Color;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Evaluator {
    private static final int INSTRUCTION_LIMIT = 1_000_000;
    private int instructionCount = 0;
    
    // Callbacks to communicate with the outside world (GUI or CLI)
    private final Consumer<String> onSay;
    private final Consumer<MoveEvent> onMove;
    private final Supplier<Boolean> stopSignal;

    public record MoveEvent(int fromX, int fromY, int toX, int toY, double newDirection, boolean isPenDown, Color color) {}

    public Evaluator(Consumer<String> onSay, Consumer<MoveEvent> onMove, Supplier<Boolean> stopSignal) {
        this.onSay = onSay;
        this.onMove = onMove;
        this.stopSignal = stopSignal;
    }

    public void evaluate(List<Statement> program, Environment env) {
        instructionCount = 0;
        // Initial state update before any commands run
        onMove.accept(new MoveEvent(env.getX(), env.getY(), env.getX(), env.getY(), env.getDirection(), env.isPenDown(), env.getPenColor()));
        for (Statement statement : program) {
            evaluateStatement(statement, env);
        }
    }

    private void evaluateStatement(Statement stmt, Environment env) {
        if (stopSignal.get()) return;
        
        if (++instructionCount > INSTRUCTION_LIMIT) {
            onSay.accept("Error: Execution timed out! Possible infinite loop.");
            return;
        }
        
        if (stmt instanceof SetStatement setStmt) {
            Object value = evaluateExpression(setStmt.value(), env);
            if (isError(value)) {
                onSay.accept((String) value);
            } else {
                env.set(setStmt.name().value(), value);
            }
        } else if (stmt instanceof MoveStatement moveStmt) {
            evaluateMoveStatement(moveStmt, env);
        } else if (stmt instanceof TurnStatement turnStmt) {
            evaluateTurnStatement(turnStmt, env);
        } else if (stmt instanceof SayStatement sayStmt) {
            evaluateSayStatement(sayStmt, env);
        } else if (stmt instanceof RepeatStatement repeatStmt) {
            evaluateRepeatStatement(repeatStmt, env);
        } else if (stmt instanceof IfStatement ifStmt) {
            evaluateIfStatement(ifStmt, env);
        } else if (stmt instanceof PenStatement penStmt) {
            evaluatePenStatement(penStmt, env);
        } else if (stmt instanceof SetColorStatement colorStmt) {
            evaluateSetColorStatement(colorStmt, env);
        } else if (stmt instanceof FunctionDefinitionStatement funcDefStmt) {
            env.defineFunction(funcDefStmt.name().value(), funcDefStmt);
        } else if (stmt instanceof FunctionCallStatement funcCallStmt) {
            evaluateFunctionCall(funcCallStmt, env);
        } else if (stmt instanceof ExpressionStatement exprStmt) {
            // Evaluate the expression but ignore the result (for standalone variable references)
            evaluateExpression(exprStmt.expression(), env);
        }
    }

    private void evaluatePenStatement(PenStatement stmt, Environment env) {
        if (stmt.state().equalsIgnoreCase("up")) {
            env.setPenDown(false);
        } else if (stmt.state().equalsIgnoreCase("down")) {
            env.setPenDown(true);
        }
        // Send a move event to update the GUI with new pen state
        onMove.accept(new MoveEvent(env.getX(), env.getY(), env.getX(), env.getY(), env.getDirection(), env.isPenDown(), env.getPenColor()));
    }

    private void evaluateSetColorStatement(SetColorStatement stmt, Environment env) {
        Object colorVal = evaluateExpression(stmt.colorName(), env);
        if (isError(colorVal)) {
            onSay.accept((String) colorVal);
            return;
        }
        
        if (!(colorVal instanceof String colorName)) {
            onSay.accept("Error: 'color' requires a string color name.");
            return;
        }
        
        Color color = parseColor(colorName);
        if (color == null) {
            onSay.accept("Error: Unknown color '" + colorName + "'. Supported colors: red, green, blue, yellow, orange, purple, black, white");
            return;
        }
        
        env.setPenColor(color);
        // Send a move event to update the GUI with new color
        onMove.accept(new MoveEvent(env.getX(), env.getY(), env.getX(), env.getY(), env.getDirection(), env.isPenDown(), env.getPenColor()));
    }

    private Color parseColor(String colorName) {
        return switch (colorName.toLowerCase()) {
            case "red" -> Color.RED;
            case "green" -> Color.GREEN;
            case "blue" -> Color.BLUE;
            case "yellow" -> Color.YELLOW;
            case "orange" -> Color.ORANGE;
            case "purple" -> new Color(128, 0, 128);
            case "black" -> Color.BLACK;
            case "white" -> Color.WHITE;
            default -> null;
        };
    }

    private void evaluateRepeatStatement(RepeatStatement stmt, Environment env) {
        Object timesVal = evaluateExpression(stmt.times(), env);
        if (!(timesVal instanceof Integer times)) {
            onSay.accept("Error: 'repeat' requires a number.");
            return;
        }
        for (int i = 0; i < times; i++) {
            if (stopSignal.get()) return;
            for (Statement bodyStatement : stmt.body()) {
                evaluateStatement(bodyStatement, env);
            }
        }
    }

    private Object evaluateExpression(Expression expr, Environment env) {
        if (expr instanceof IntegerLiteral i) {
            return i.value();
        }
        if (expr instanceof StringLiteral s) {
            return s.value();
        }
        if (expr instanceof Identifier id) {
            Object value = env.get(id.value());
            if (value == null) {
                return "Error: variable '" + id.value() + "' not found.";
            }
            return value;
        }
        if (expr instanceof InfixExpression infix) {
            Object left = evaluateExpression(infix.left(), env);
            if (isError(left)) return left;
            Object right = evaluateExpression(infix.right(), env);
            if (isError(right)) return right;
            // Handle string concatenation
            if (left instanceof String || right instanceof String) {
                return String.valueOf(left) + String.valueOf(right);
            }
            if (left instanceof Integer l && right instanceof Integer r) {
                return switch (infix.operator()) {
                    case "+" -> l + r;
                    case "-" -> l - r;
                    case "*" -> l * r;
                    case "/" -> (r == 0) ? "Error: Division by zero" : l / r;
                    case "==" -> l.equals(r);
                    case "!=" -> !l.equals(r);
                    case ">" -> l > r;
                    case "<" -> l < r;
                    default -> "Error: Unknown operator '" + infix.operator() + "' for numbers.";
                };
            }
            return "Error: Cannot perform operation '" + infix.operator() + "' on these types.";
        }
        if (expr instanceof ListLiteral listLiteral) {
            List<Object> elements = new ArrayList<>();
            for (Expression el : listLiteral.elements()) {
                Object evaluated = evaluateExpression(el, env);
                if (isError(evaluated)) return evaluated;
                elements.add(evaluated);
            }
            return elements;
        }
        if (expr instanceof IndexExpression indexExpr) {
            Object left = evaluateExpression(indexExpr.left(), env);
            if (isError(left)) return left;
            
            Object index = evaluateExpression(indexExpr.index(), env);
            if (isError(index)) return index;
            
            if (!(left instanceof List)) {
                return "Error: index operator [] cannot be used on non-list type.";
            }
            if (!(index instanceof Integer)) {
                return "Error: index must be a number.";
            }

            List<Object> list = (List<Object>) left;
            int idx = (Integer) index;

            if (idx < 0 || idx >= list.size()) {
                return "Error: index " + idx + " out of bounds for list of size " + list.size() + ".";
            }
            return list.get(idx);
        }
        return "Error: Cannot evaluate expression";
    }
    
    // Helper to check for error objects
    private boolean isError(Object obj) {
        return obj instanceof String s && s.startsWith("Error:");
    }
    
    private void evaluateMoveStatement(MoveStatement stmt, Environment env) {
        Object stepsVal = evaluateExpression(stmt.steps(), env);
        if (!(stepsVal instanceof Integer steps)) {
            onSay.accept("Error: 'move forward' requires a number. Got: " + stepsVal);
            return;
        }
        
        int oldX = env.getX();
        int oldY = env.getY();
        int newX = oldX + (int) (steps * Math.sin(Math.toRadians(env.getDirection())));
        int newY = oldY - (int) (steps * Math.cos(Math.toRadians(env.getDirection())));

        env.setPosition(newX, newY);
        onMove.accept(new MoveEvent(oldX, oldY, newX, newY, env.getDirection(), env.isPenDown(), env.getPenColor()));
    }

    private void evaluateTurnStatement(TurnStatement stmt, Environment env) {
        Object degreesVal = evaluateExpression(stmt.degrees(), env);
        if (!(degreesVal instanceof Integer degrees)) {
            onSay.accept("Error: 'turn' requires a number. Got: " + degreesVal);
            return;
        }

        if (stmt.direction().equalsIgnoreCase("right")) {
            env.setDirection((env.getDirection() + degrees) % 360);
        } else if (stmt.direction().equalsIgnoreCase("left")) {
            env.setDirection((env.getDirection() - degrees + 360) % 360);
        }
        
        onMove.accept(new MoveEvent(env.getX(), env.getY(), env.getX(), env.getY(), env.getDirection(), env.isPenDown(), env.getPenColor()));
    }

    private void evaluateSayStatement(SayStatement stmt, Environment env) {
        Object messageVal = evaluateExpression(stmt.message(), env);
        if (isError(messageVal)) {
            onSay.accept((String)messageVal);
        } else {
            onSay.accept(String.valueOf(messageVal));
        }
    }

    private void evaluateIfStatement(IfStatement stmt, Environment env) {
        Object condition = evaluateExpression(stmt.condition(), env);
        if (isError(condition)) {
            onSay.accept((String) condition);
            return;
        }
        boolean isTruthy = true;
        if (condition instanceof Boolean b) {
            isTruthy = b;
        }
        if (isTruthy) {
            for (Statement bodyStmt : stmt.consequence()) {
                evaluateStatement(bodyStmt, env);
            }
        } else if (stmt.alternative() != null) {
            for (Statement bodyStmt : stmt.alternative()) {
                evaluateStatement(bodyStmt, env);
            }
        }
    }

    private void evaluateFunctionCall(FunctionCallStatement call, Environment env) {
        FunctionDefinitionStatement funcDef = env.getFunction(call.function().value());
        
        if (funcDef == null) {
            onSay.accept("Error: Function '" + call.function().value() + "' is not defined.");
            return;
        }
        
        if (call.arguments().size() != funcDef.parameters().size()) {
            onSay.accept("Error: Function '" + funcDef.name().value() + "' expects " +
                         funcDef.parameters().size() + " arguments, but got " + call.arguments().size() + ".");
            return;
        }

        // 1. Create a new, scoped environment for the function
        Environment functionEnv = new Environment(env);

        // 2. Evaluate arguments in the *calling* environment and bind them to parameters in the *new* environment
        for (int i = 0; i < funcDef.parameters().size(); i++) {
            Identifier param = funcDef.parameters().get(i);
            Expression arg = call.arguments().get(i);
            Object argValue = evaluateExpression(arg, env);
            if (isError(argValue)) {
                onSay.accept((String) argValue);
                return;
            }
            functionEnv.set(param.value(), argValue);
        }

        // 3. Execute the function body in the new, scoped environment
        for (Statement bodyStatement : funcDef.body()) {
            if (stopSignal.get()) return;
            evaluateStatement(bodyStatement, functionEnv);
        }
    }
}