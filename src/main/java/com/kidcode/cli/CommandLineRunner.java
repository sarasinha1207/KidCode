package com.kidcode.cli;

import com.kidcode.core.KidCodeEngine;

public class CommandLineRunner {
    public static void main(String[] args) {
        String sourceCode = """
                say \"Running from the command line!\"
                move forward 100
                turn right 90
                move forward 50
                say \"Done.\"
                """;

        KidCodeEngine engine = new KidCodeEngine();
        
        System.out.println("--- Executing KidCode Script ---");
        
        engine.execute(sourceCode,
                (message) -> System.out.println("Cody says: " + message),
                (event) -> System.out.println("Cody moved to (" + event.toX() + ", " + event.toY() + ")"),
                (errors) -> errors.forEach(err -> System.err.println("ERROR: " + err))
        );
        
        System.out.println("--- Script Finished ---");
    }
} 