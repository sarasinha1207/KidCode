package com.kidcode.cli;

import com.kidcode.core.KidCodeEngine;
import com.kidcode.core.event.ExecutionEvent;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.List;

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
        
        List<ExecutionEvent> events = engine.execute(sourceCode);
        
        for (ExecutionEvent event : events) {
            if (event instanceof ExecutionEvent.SayEvent e) {
                System.out.println("Cody says: " + e.message());
            } else if (event instanceof ExecutionEvent.MoveEvent e) {
                System.out.println("Cody moved to (" + e.toX() + ", " + e.toY() + ")");
            } else if (event instanceof ExecutionEvent.ErrorEvent e) {
                System.err.println("ERROR: " + e.errorMessage());
            }
        }
        
        System.out.println("--- Script Finished ---");
    }
} 