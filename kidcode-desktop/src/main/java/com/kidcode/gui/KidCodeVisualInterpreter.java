package com.kidcode.gui;

import com.kidcode.core.KidCodeEngine;
import com.kidcode.core.evaluator.Evaluator;
import com.kidcode.core.event.ExecutionEvent;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane;
import java.awt.Color;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

public class KidCodeVisualInterpreter extends JFrame {
    private final DrawingPanel drawingPanel;
    private final RSyntaxTextArea codeArea;
    private final JTextArea outputArea;
    private final KidCodeEngine engine;

    public KidCodeVisualInterpreter() {
        this.engine = new KidCodeEngine();
        
        setTitle("KidCode Visual Interpreter");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        drawingPanel = new DrawingPanel();
        add(drawingPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new BorderLayout());
        codeArea = new RSyntaxTextArea(20, 40);
        codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE);
        codeArea.setCodeFoldingEnabled(true);
        codeArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        // Add some default code for testing
        codeArea.setText("""
                move forward 100
                turn right 90
                move forward 50
                say \"Hello, Structured World!\"
                turn left 45
                move forward 75
                """);
        RTextScrollPane codeScrollPane = new RTextScrollPane(codeArea);
        controlPanel.add(codeScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton runButton = new JButton("Run Code");
        runButton.addActionListener(e -> new Thread(this::runCode).start());
        buttonPanel.add(runButton);
        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> engine.stopExecution());
        buttonPanel.add(stopButton);
        controlPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(controlPanel, BorderLayout.WEST);

        outputArea = new JTextArea(5, 30);
        outputArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        add(outputScrollPane, BorderLayout.SOUTH);

        // Add menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem openItem = new JMenuItem("Open...");
        openItem.addActionListener(e -> openFile());
        fileMenu.add(openItem);
        
        JMenuItem saveItem = new JMenuItem("Save As...");
        saveItem.addActionListener(e -> saveFile());
        fileMenu.add(saveItem);
        
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    private Color parseAwtColor(String colorName) {
        return switch (colorName.toLowerCase()) {
            case "red" -> Color.RED;
            case "green" -> Color.GREEN;
            case "blue" -> Color.BLUE;
            case "yellow" -> Color.YELLOW;
            case "orange" -> Color.ORANGE;
            case "purple" -> new Color(128, 0, 128);
            case "black" -> Color.BLACK;
            case "white" -> Color.WHITE;
            default -> Color.BLACK;
        };
    }

    private void runCode() {
        String code = codeArea.getText();
        List<ExecutionEvent> events = engine.execute(code);
        SwingUtilities.invokeLater(() -> {
            for (ExecutionEvent event : events) {
                if (event instanceof ExecutionEvent.ClearEvent) {
            drawingPanel.clear();
            outputArea.setText("");
                } else if (event instanceof ExecutionEvent.MoveEvent e) {
                    if (e.isPenDown() && (e.fromX() != e.toX() || e.fromY() != e.toY())) {
                        drawingPanel.drawLine(e.fromX(), e.fromY(), e.toX(), e.toY(), parseAwtColor(e.color()));
                    }
                    drawingPanel.updateCodyState(e.toX(), e.toY(), e.newDirection());
                } else if (event instanceof ExecutionEvent.SayEvent e) {
                    outputArea.setForeground(Color.BLACK);
                    outputArea.append("Cody says: " + e.message() + "\n");
                } else if (event instanceof ExecutionEvent.ErrorEvent e) {
                    outputArea.setForeground(Color.RED);
                    outputArea.append("ERROR: " + e.errorMessage() + "\n");
                }
            }
        });
    }
    
    private void openFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                codeArea.setText(new String(java.nio.file.Files.readAllBytes(chooser.getSelectedFile().toPath())));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error opening file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.nio.file.Files.write(chooser.getSelectedFile().toPath(), codeArea.getText().getBytes());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // --- Inner classes from your original file ---
    static class DrawingPanel extends JPanel {
        private final List<Line> lines = new ArrayList<>();
        // Add state for Cody's visual representation
        private int codyX = 250;
        private int codyY = 250;
        private double codyDirection = 0; // In degrees

        public DrawingPanel() {
            setPreferredSize(new Dimension(500, 500));
            setBackground(Color.WHITE);
        }

        public void drawLine(int x1, int y1, int x2, int y2, Color color) {
            lines.add(new Line(x1, y1, x2, y2, color));
            // We don't need to call repaint() here, updateCodyState will do it.
        }

        public void updateCodyState(int x, int y, double direction) {
            this.codyX = x;
            this.codyY = y;
            this.codyDirection = direction;
            repaint(); // Trigger a repaint to show Cody's new position/rotation
        }

        public void clear() {
            lines.clear();
            // Reset Cody to the initial state when clearing
            this.codyX = 250;
            this.codyY = 250;
            this.codyDirection = 0;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // 1. Draw all the lines from previous moves
            for (Line line : lines) {
                g2d.setColor(line.color);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(line.x1, line.y1, line.x2, line.y2);
            }

            // 2. Draw Cody
            drawCody(g2d);
        }

        private void drawCody(Graphics2D g2d) {
            // Save the current graphics state (so our transformations don't affect other drawings)
            Graphics2D g2dCopy = (Graphics2D) g2d.create();

            // Create a shape for Cody (a triangle pointing up)
            // This shape is defined around the origin (0,0)
            Polygon codyShape = new Polygon();
            codyShape.addPoint(0, -15);  // Tip of the triangle
            codyShape.addPoint(10, 10); // Bottom right
            codyShape.addPoint(-10, 10);// Bottom left

            // --- Perform transformations to place and rotate Cody ---
            // a. Move the coordinate system to Cody's position
            g2dCopy.translate(codyX, codyY);
            // b. Rotate the coordinate system to match Cody's direction
            g2dCopy.rotate(Math.toRadians(codyDirection));
            
            // --- Draw the shape ---
            // The shape is now drawn at the translated and rotated origin
            g2dCopy.setColor(new Color(255, 100, 0)); // A nice orange color
            g2dCopy.fill(codyShape);
            g2dCopy.setColor(Color.BLACK);
            g2dCopy.draw(codyShape);

            // Restore the original graphics state
            g2dCopy.dispose();
        }

        static class Line {
            int x1, y1, x2, y2;
            Color color;
            Line(int x1, int y1, int x2, int y2, Color color) {
                this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2; this.color = color;
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new KidCodeVisualInterpreter().setVisible(true));
    }
} 