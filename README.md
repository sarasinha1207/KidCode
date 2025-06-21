# KidCode Visual Interpreter

A powerful, educational programming language and visual interpreter designed to teach programming concepts through visual feedback. KidCode allows users to control a character named "Cody" to draw shapes, create patterns, and learn programming fundamentals.

## Features

### 🎯 Core Language Features
- **Variables**: Store and manipulate values with `set` commands
- **Arithmetic**: Full mathematical expressions with `+`, `-`, `*`, `/`
- **Loops**: Repeat blocks of code with `repeat` statements
- **Conditionals**: Make decisions with `if`/`else` statements
- **String Operations**: Concatenate strings and display messages

### 🎨 Visual Features
- **Pen Control**: Lift and lower the pen with `pen up` and `pen down`
- **Color Support**: Change drawing colors with `color` command
- **Real-time Visualization**: Watch Cody move and draw in real-time
- **Visual Feedback**: See the character rotate and move according to commands

### 🛠️ User Interface
- **Modern Code Editor**: Syntax highlighting and professional editing experience
- **File Management**: Open and save KidCode scripts
- **Stop Button**: Halt execution at any time
- **Error Handling**: Clear error messages for debugging
- **Output Panel**: See program output and messages

### 🔒 Safety Features
- **Execution Timeout**: Automatic protection against infinite loops
- **Error Recovery**: Graceful handling of syntax and runtime errors
- **Thread Safety**: Responsive UI that doesn't freeze during execution

## Installation & Running

### Prerequisites
- Java 17 or higher
- Maven (for development)

### Quick Start
1. **Download the JAR file**: `kidcode-structured-1.0-SNAPSHOT-jar-with-dependencies.jar`
2. **Run the application**:
   ```bash
   java -jar kidcode-structured-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```
3. **Or double-click** the JAR file on most systems

### From Source
```bash
git clone <repository-url>
cd kidcode-structured
mvn clean package
java -jar target/kidcode-structured-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Language Reference

### Basic Commands

| Command | Syntax | Description |
|---------|--------|-------------|
| `move forward` | `move forward <number>` | Move Cody forward by the specified number of pixels |
| `turn` | `turn left/right <degrees>` | Turn Cody left or right by the specified degrees |
| `say` | `say "message"` | Display a message in the output panel |

### Variables and Expressions

| Command | Syntax | Description |
|---------|--------|-------------|
| `set` | `set variable = expression` | Assign a value to a variable |
| Arithmetic | `5 + 3`, `x * 2`, `(10 - 5) / 2` | Mathematical expressions |
| String Concatenation | `"Hello" + " " + "World"` | Combine strings |

### Control Flow

| Command | Syntax | Description |
|---------|--------|-------------|
| `repeat` | `repeat <times> ... end repeat` | Execute a block of code multiple times |
| `if` | `if condition ... else ... end if` | Conditional execution |

### Visual Commands

| Command | Syntax | Description |
|---------|--------|-------------|
| `pen up` | `pen up` | Lift the pen (no drawing) |
| `pen down` | `pen down` | Lower the pen (drawing enabled) |
| `color` | `color "color_name"` | Change the drawing color |

### Supported Colors
- `red`, `green`, `blue`, `yellow`, `orange`, `purple`, `black`, `white`

### Comparison Operators
- `==` (equal), `!=` (not equal), `>` (greater than), `<` (less than)

## Example Programs

### Simple Square
```
repeat 4
    move forward 100
    turn right 90
end repeat
```

### Rainbow Spiral
```
set size = 10
set colors = "red green blue yellow orange purple"

repeat 20
    color "red"
    move forward size
    turn right 90
    set size = size + 5
end repeat
```

### Interactive Drawing
```
set message = "Hello" + " " + "World"
say message

color "blue"
pen down
move forward 100
turn right 90

pen up
move forward 50
pen down

color "green"
move forward 100
```

### Conditional Pattern
```
set size = 20

repeat 10
    if size > 30
        color "red"
        say "Big shape!"
    else
        color "blue"
        say "Small shape!"
    end if
    
    move forward size
    turn right 90
    set size = size + 5
end repeat
```

## Architecture

KidCode is built with a clean, modular architecture:

- **Lexer**: Tokenizes source code into meaningful units
- **Parser**: Builds an Abstract Syntax Tree (AST) from tokens
- **Evaluator**: Executes the AST and manages program state
- **GUI**: Provides the user interface and visual feedback

### Key Components
- `KidCodeEngine`: Main orchestrator that coordinates lexing, parsing, and evaluation
- `Environment`: Manages program state (variables, position, pen state, colors)
- `DrawingPanel`: Handles visual rendering of Cody and the drawing canvas

## Development

### Project Structure
```
kidcode-structured/
├── src/main/java/com/kidcode/
│   ├── core/
│   │   ├── lexer/          # Tokenization
│   │   ├── parser/         # AST construction
│   │   ├── ast/           # Abstract Syntax Tree nodes
│   │   └── evaluator/     # Program execution
│   ├── gui/               # User interface
│   └── cli/               # Command-line interface
├── src/test/              # Unit tests
└── pom.xml               # Maven configuration
```

### Building
```bash
mvn clean compile          # Compile only
mvn clean test            # Run tests
mvn clean package         # Create JAR files
mvn exec:java            # Run with Maven
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new features
5. Submit a pull request

## License

This project is open source and available under the MIT License.

## Acknowledgments

- Built with Java 17 and Swing for cross-platform compatibility
- Uses RSyntaxTextArea for enhanced code editing
- Maven for build management and dependency resolution

---

**Happy Coding with KidCode!** 🎨✨ 