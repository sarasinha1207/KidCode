package com.kidcode.core.evaluator;

import java.util.HashMap;
import java.util.Map;
import java.awt.Color;
import com.kidcode.core.ast.FunctionDefinitionStatement;

// Holds the state of our "Cody" character.
public class Environment {
    private int x = 250;
    private int y = 250;
    private double direction = 0; // 0=North, 90=East, 180=South, 270=West

    // Add a map to store variables
    private final Map<String, Object> store = new HashMap<>();
    
    // NEW: Function storage
    private final Map<String, FunctionDefinitionStatement> functions = new HashMap<>();
    
    // NEW: Link to the outer scope for lexical scoping
    private final Environment outer;
    
    // Add pen state and color tracking
    private boolean isPenDown = true;
    private Color penColor = Color.BLUE;

    // Global environment constructor
    public Environment() {
        this.outer = null;
    }

    // Scoped environment constructor
    public Environment(Environment outer) {
        this.outer = outer;
    }

    // Turtle state should always be controlled by the global environment
    public int getX() { return (outer != null) ? outer.getX() : x; }
    public int getY() { return (outer != null) ? outer.getY() : y; }
    public double getDirection() { return (outer != null) ? outer.getDirection() : direction; }

    public void setPosition(int x, int y) {
        if (outer != null) outer.setPosition(x, y); else { this.x = x; this.y = y; }
    }

    public void setDirection(double direction) {
        if (outer != null) outer.setDirection(direction); else { this.direction = direction; }
    }

    // Variable access now respects scope
    public Object get(String name) {
        if (store.containsKey(name)) {
            return store.get(name);
        }
        if (outer != null) {
            return outer.get(name);
        }
        return null;
    }

    public void set(String name, Object value) {
        store.put(name, value);
    }
    
    // Function definitions are global
    public FunctionDefinitionStatement getFunction(String name) {
        if (functions.containsKey(name)) {
            return functions.get(name);
        }
        if (outer != null) {
            return outer.getFunction(name);
        }
        return null;
    }
    
    public void defineFunction(String name, FunctionDefinitionStatement func) {
        if (outer != null) {
            outer.defineFunction(name, func);
        } else {
            functions.put(name, func);
        }
    }
    
    // Pen state methods
    public boolean isPenDown() { return (outer != null) ? outer.isPenDown() : isPenDown; }
    public void setPenDown(boolean isPenDown) { 
        if (outer != null) outer.setPenDown(isPenDown); else { this.isPenDown = isPenDown; }
    }
    
    // Color methods
    public Color getPenColor() { return (outer != null) ? outer.getPenColor() : penColor; }
    public void setPenColor(Color penColor) { 
        if (outer != null) outer.setPenColor(penColor); else { this.penColor = penColor; }
    }
} 