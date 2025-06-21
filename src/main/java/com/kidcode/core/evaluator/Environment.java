package com.kidcode.core.evaluator;

import java.util.HashMap;
import java.util.Map;
import java.awt.Color;

// Holds the state of our "Cody" character.
public class Environment {
    private int x = 250;
    private int y = 250;
    private double direction = 0; // 0=North, 90=East, 180=South, 270=West

    // Add a map to store variables
    private final Map<String, Object> store = new HashMap<>();
    
    // Add pen state and color tracking
    private boolean isPenDown = true;
    private Color penColor = Color.BLUE;

    public int getX() { return x; }
    public int getY() { return y; }
    public double getDirection() { return direction; }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setDirection(double direction) {
        this.direction = direction;
    }

    public void set(String name, Object value) {
        store.put(name, value);
    }

    public Object get(String name) {
        return store.get(name);
    }
    
    // Pen state methods
    public boolean isPenDown() { return isPenDown; }
    public void setPenDown(boolean isPenDown) { this.isPenDown = isPenDown; }
    
    // Color methods
    public Color getPenColor() { return penColor; }
    public void setPenColor(Color penColor) { this.penColor = penColor; }
} 