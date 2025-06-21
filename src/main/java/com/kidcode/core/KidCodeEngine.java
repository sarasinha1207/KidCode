package com.kidcode.core;

import com.kidcode.core.ast.Statement;
import com.kidcode.core.evaluator.Environment;
import com.kidcode.core.evaluator.Evaluator;
import com.kidcode.core.lexer.Lexer;
import com.kidcode.core.parser.Parser;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class KidCodeEngine {

    private volatile boolean executionStopped = false;

    public void stopExecution() {
        this.executionStopped = true;
    }

    public void execute(String sourceCode, Consumer<String> onSay, Consumer<Evaluator.MoveEvent> onMove, Consumer<List<String>> onError) {
        this.executionStopped = false;

        Lexer lexer = new Lexer(sourceCode);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        List<String> errors = parser.getErrors();
        if (!errors.isEmpty()) {
            onError.accept(errors);
            return;
        }

        // This is where the stop signal is created from the internal flag
        Supplier<Boolean> stopSignal = () -> executionStopped;

        // And this is where the new Evaluator is called with the correct 3 arguments
        Evaluator evaluator = new Evaluator(onSay, onMove, stopSignal);
        Environment environment = new Environment();
        evaluator.evaluate(program, environment);
    }
}