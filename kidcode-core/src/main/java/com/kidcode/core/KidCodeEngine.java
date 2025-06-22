package com.kidcode.core;

import com.kidcode.core.ast.Statement;
import com.kidcode.core.evaluator.Environment;
import com.kidcode.core.evaluator.Evaluator;
import com.kidcode.core.event.ExecutionEvent;
import com.kidcode.core.lexer.Lexer;
import com.kidcode.core.parser.Parser;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class KidCodeEngine {

    private volatile boolean executionStopped = false;

    public void stopExecution() {
        this.executionStopped = true;
    }

    public List<ExecutionEvent> execute(String sourceCode) {
        this.executionStopped = false;

        Lexer lexer = new Lexer(sourceCode);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        List<String> errors = parser.getErrors();
        if (!errors.isEmpty()) {
            List<ExecutionEvent> errorEvents = new ArrayList<>();
            errors.forEach(err -> errorEvents.add(new ExecutionEvent.ErrorEvent(err)));
            return errorEvents;
        }

        Supplier<Boolean> stopSignal = () -> executionStopped;
        Evaluator evaluator = new Evaluator(stopSignal);
        Environment environment = new Environment();

        return evaluator.evaluate(program, environment);
    }
}