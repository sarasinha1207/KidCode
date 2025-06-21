# KidCode Visual Interpreter 🎨

**KidCode** is a powerful, educational programming language and visual interpreter designed to teach core programming concepts through instant visual feedback. With KidCode, you can control a turtle named "Cody" to draw shapes, create patterns, and learn programming fundamentals in an interactive and fun way.

The language is designed to be simple enough for beginners but powerful enough to create complex artwork, thanks to features like **variables, loops, conditionals, functions, and lists**.


*(Suggestion: Replace the image URL above with a real screenshot of your application!)*

## ✨ Features

### 🎯 Core Language Features
- **Variables**: Store and manipulate values with `set` commands.
- **Arithmetic**: Full mathematical expressions with `+`, `-`, `*`, `/`.
- **Loops**: Repeat blocks of code with `repeat` statements.
- **Conditionals**: Make decisions with `if`/`else` statements.
- **Functions**: Define your own reusable commands to create complex logic.
- **Lists**: Store and access collections of data like colors or sizes.
- **String Operations**: Concatenate strings and display messages.

### 🎨 Visual & UI Features
- **Real-time Visualization**: Watch Cody move and draw your designs in real-time.
- **Pen Control**: Lift and lower the pen with `pen up` and `pen down`.
- **Color Support**: Change drawing colors with the `color` command.
- **Modern Code Editor**: Professional editing experience with syntax highlighting.
- **File Management**: Open and save your KidCode scripts.
- **Execution Control**: A `Stop` button to halt execution at any time.
- **Clear Feedback**: An output panel displays messages and error diagnostics.

### 🔒 Safety & Performance
- **Execution Timeout**: Protects against accidental infinite loops.
- **Graceful Error Handling**: Clear, line-numbered syntax and runtime error messages.
- **Responsive UI**: The UI remains responsive while code is executing.

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven (for building from source)

### Run the Application
1. **Download the latest JAR file** from the project's releases page (e.g., `kidcode-structured-1.0-SNAPSHOT-jar-with-dependencies.jar`).
2. Run the application from your terminal:
   ```bash
   java -jar kidcode-structured-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```

On most desktop systems, you can simply double-click the JAR file to run it.

### Build from Source
```bash
# Clone the repository
git clone <repository-url>
cd sansi-28-kidcode

# Build the project using Maven
mvn clean package

# Run the newly created JAR file
java -jar target/kidcode-structured-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## 📚 Language Reference

### Basic Commands
| Command | Syntax | Description |
|---------|--------|-------------|
| move forward | `move forward <value>` | Moves Cody forward by a number of pixels. |
| turn | `turn <left/right> <degrees>` | Turns Cody left or right. |
| say | `say "message"` or `<variable>` | Displays a message in the output panel. |
| pen | `pen <up/down>` | Lifts or lowers the drawing pen. |
| color | `color "color_name"` | Changes the drawing color. |

### Variables & Expressions
| Concept | Syntax | Description |
|---------|--------|-------------|
| set | `set variable = <expression>` | Assigns a value or expression to a variable. |
| Arithmetic | `5 + 3`, `x * 2`, `(10 - 5) / 2` | Full support for mathematical expressions. |
| Comparisons | `==`, `!=`, `>`, `<` | Used inside if conditions. |

### Data Structures: Lists

Lists allow you to store multiple values in a single variable. They are created with square brackets `[]`.

| Operation | Syntax | Description |
|-----------|--------|-------------|
| Creation | `set my_list = [value1, value2, ...]` | Creates a new list. |
| Access | `my_list[index]` | Gets an element from the list. Note: The first element is at index 0! |

**Example:**
```kidcode
set colors = ["red", "green", "blue"]
say colors[0] # says "red"
color colors[1] # sets color to green
```

### Control Flow
| Command | Syntax | Description |
|---------|--------|-------------|
| repeat | `repeat <times> ... end repeat` | Executes a block of code multiple times. |
| if | `if <condition> ... else ... end if` | Conditional execution. The else block is optional. |

### Reusable Logic: Functions

Functions let you define your own custom commands to build more complex and readable programs.

| Operation | Syntax | Description |
|-----------|--------|-------------|
| Definition | `define <name> [param1] [param2] ... end define` | Creates a new command. Parameters are optional. |
| Calling | `<name> [arg1] [arg2]` | Executes the function you defined. |

**Example:**
```kidcode
# Define a function to draw a square
define draw_square size
    repeat 4
        move forward size
        turn right 90
    end repeat
end define

# Use the new command
draw_square 50
color "red"
draw_square 100
```

## 💡 Example Programs

### Colorful Starburst

This program uses a list to store colors and draws two overlapping, multi-colored triangles to create a star-like shape.

```kidcode
set colors = ["red", "green", "orange", "blue", "black", "purple"]
set i = 0

repeat 4
    set i = 0
    repeat 3
        color colors[i]
        move forward 100
        turn right 120
        set i = i + 1
    end repeat

    turn right 30

    set i = 3
    repeat 3
        color colors[i]
        move forward 100
        if i == 5
            turn right 180
        else
            turn right 120
        end if
        set i = i + 1
    end repeat
end repeat

```

### A Village of Houses (using a function)

This example shows the power of functions by defining a house command and then using it to draw a row of houses.

```kidcode
define house size
    pen down
    color "blue"
    repeat 4
        move forward size
        turn right 90
    end repeat
    
    pen up
    
    color "red"
    move forward size

    pen down
    turn right 30
    move forward size
    turn right 120
    move forward size
    turn right 120
    move forward size

    pen up
    turn left 90
    move forward size/2
    turn left 90
    move forward size/3
 
    pen down
    move forward size/3
    turn right 90
    move forward size/2
    turn right 90
    move forward size/3
    turn right 90
    move forward size/2

    pen up
    turn right 90
    move forward size*2
    turn right 90
    move forward size/2
    turn left 180
    pen down
end define


house 60

say "house 1"

house 90

say "house 2"

```

## 🏛️ Architecture

KidCode is built with a clean, modular architecture that processes source code in several stages:

1. **Lexer**: Tokenizes the source code into a stream of meaningful units (e.g., MOVE, NUMBER, IDENTIFIER).
2. **Parser**: Constructs an Abstract Syntax Tree (AST) from the token stream, representing the program's structure.
3. **Evaluator**: Traverses the AST to execute the program, manage state (variables, functions, Cody's position), and trigger visual updates.
4. **GUI**: Provides the user interface, code editor, and the drawing canvas that visualizes the evaluator's output.

## 🤝 Contributing

Contributions are welcome! If you'd like to help improve KidCode, please follow these steps:

1. Fork the repository.
2. Create a new feature branch (`git checkout -b feature/amazing-new-feature`).
3. Make your changes and commit them (`git commit -m 'Add some amazing feature'`).
4. Push to the branch (`git push origin feature/amazing-new-feature`).
5. Open a Pull Request.

## 📄 License

This project is open source and available under the MIT License.

---

**Happy Coding with KidCode!** 🎨✨ 
