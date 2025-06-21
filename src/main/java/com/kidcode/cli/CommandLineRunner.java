package com.kidcode.cli;

import com.kidcode.core.KidCodeEngine;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class CommandLineRunner {
    public static void main(String[] args) {
        String sourceCode;
        
        if (args.length > 0) {
            // Read from file
            try {
                sourceCode = Files.readString(Paths.get(args[0]));
            } catch (IOException e) {
                System.err.println("Error reading file: " + e.getMessage());
                return;
            }
        } else {
            // Default script
            sourceCode = """
                    say \"Running from the command line!\"
                    move forward 100
                    turn right 90
                    move forward 50
                    say \"Done.\"
                    """;
        }

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