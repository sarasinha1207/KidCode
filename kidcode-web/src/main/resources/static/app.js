// File: kidcode-web/src/main/resources/static/app.js

// --- 1. GET REFERENCES TO OUR HTML ELEMENTS ---
const runButton = document.getElementById("run-button");
const editorContainer = document.getElementById("editor-container");
const drawingCanvas = document.getElementById("drawing-canvas");
const outputArea = document.getElementById("output-area");
const ctx = drawingCanvas.getContext("2d");
const helpButton = document.getElementById("help-button");
const helpModal = document.getElementById("help-modal");
const closeButton = document.querySelector(".close-button");

// --- MONACO: Global variable to hold the editor instance ---
let editor;
let validationTimeout;
let validationController;

// --- MONACO: Function to define and register our custom language ---
function registerKidCodeLanguage() {
  monaco.languages.register({ id: "kidcode" });

  monaco.languages.setMonarchTokensProvider("kidcode", {
    keywords: [
      "move",
      "forward",
      "turn",
      "left",
      "right",
      "say",
      "repeat",
      "end",
      "set",
      "if",
      "else",
      "define",
      "pen",
      "up",
      "down",
      "color",
    ],
    tokenizer: {
      root: [
        [
          /[a-zA-Z_][\w_]*/,
          {
            cases: {
              "@keywords": "keyword",
              "@default": "identifier",
            },
          },
        ],
        [/-?\d+(?:\.\d+)?/, "number"], // supports decimals & negatives
        [/#.*$/, "comment"],
        [/"([^"\\]|\\.)*$/, "string.invalid"],
        [/"/, { token: "string.quote", bracket: "@open", next: "@string" }],
      ],
      string: [
        [/[^\\"]+/, "string"],
        [/\\./, "string.escape"], // valid generic escapes
        [/"/, { token: "string.quote", bracket: "@close", next: "@pop" }],
      ],
    },
  });
}

// --- MONACO: Use the loader to configure and create the editor ---
require.config({
  paths: { vs: "https://cdn.jsdelivr.net/npm/monaco-editor@0.34.1/min/vs" },
});

require(["vs/editor/editor.main"], function () {
  registerKidCodeLanguage();

  editor = monaco.editor.create(editorContainer, {
    value: [
      "# Welcome to KidCode!",
      "# Run this code to see a rainbow spiral, then try changing it!",
      "",
      'set colors = ["red", "orange", "yellow", "green", "blue", "purple"]',
      "set length = 5",
      "set color_index = 0",
      "",
      "# Repeat many times to make a large spiral",
      "repeat 75",
      "    # Set the color from the list",
      "    color colors[color_index]",
      "    ",
      "    move forward length",
      "    turn right 60",
      "    ",
      "    # Get ready for the next line",
      "    set length = length + 2",
      "    set color_index = color_index + 1",
      "    ",
      "    # Reset color index to loop through the rainbow",
      "    if color_index == 6",
      "        set color_index = 0",
      "    end if",
      "end repeat",
    ].join("\n"),
    language: "kidcode",
    theme: "vs-light",
    automaticLayout: true,
    fontSize: 14,
    minimap: { enabled: false },
  });

  // ✅ Safely initialize examples dropdown without breaking editor setup
  initializeExamples();

  // Add an editor action / keybinding so Ctrl/Cmd+Enter triggers the Run button
  editor.addAction({
    id: "kidcode.run",
    label: "Run KidCode (Ctrl/Cmd+Enter)",
    keybindings: [monaco.KeyMod.CtrlCmd | monaco.KeyCode.Enter],
    run: function () {
      runButton.click();
      return null;
    },
  });

  // Monaco live validation
  editor.onDidChangeModelContent(() => {
    clearTimeout(validationTimeout);
    validationTimeout = setTimeout(validateCode, 500);
  });
  validateCode();
});

// --- Initialize example dropdown (gracefully degrades if examples.js missing) ---
function initializeExamples() {
  const selector = document.getElementById("exampleSelector");
  if (!selector) {
    console.error("Example selector element not found");
    return;
  }

  if (!window.examples) {
    console.warn("examples.js not loaded - example selector disabled");
    selector.disabled = true;
    selector.innerHTML = '<option value="">Examples unavailable</option>';
    return;
  }

  Object.keys(window.examples).forEach((exampleName) => {
    const option = document.createElement("option");
    option.value = exampleName;
    option.textContent = exampleName;
    selector.appendChild(option);
  });

  selector.addEventListener("change", () => {
    const selected = selector.value;
    if (window.examples[selected]) {
      editor.setValue(window.examples[selected]);
      logToOutput(`✅ Loaded example: ${selected}`);
    }
  });
}

// --- 2. ADD EVENT LISTENER TO THE RUN BUTTON ---
runButton.addEventListener("click", async () => {
  const code = editor.getValue();

  // 🧹 Ensure a clean slate even if backend omits ClearEvent
  drawnLines = [];
  codyState = { x: 250, y: 250, direction: 0, color: "blue" };
  clearCanvas();
  outputArea.textContent = "";

  try {
    const response = await fetch("/api/execute", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ code }),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const events = await response.json();
    renderEvents(events);
  } catch (error) {
    logToOutput(`Network or server error: ${error.message}`, "error");
  }
});

// --- NEW: Function to handle validation ---
async function validateCode() {
  const code = editor.getValue();

  // Cancel any in-flight validation to prevent stale results
  if (validationController) {
    validationController.abort();
  }
  const controller = new AbortController();
  validationController = controller;

  try {
    const response = await fetch("/api/validate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ code }),
      signal: controller.signal,
    });

    if (!response.ok) {
      throw new Error(`HTTP error ${response.status}`);
    }

    const errors = await response.json();
    const model = editor.getModel();
    const markers = (Array.isArray(errors) ? errors : []).map((err) => {
      const line = Number(err.lineNumber) > 0 ? Number(err.lineNumber) : 1;
      const endCol = model ? model.getLineMaxColumn(line) : 100;
      return {
        message: String(err.message || "Syntax error"),
        severity: monaco.MarkerSeverity.Error,
        startLineNumber: line,
        endLineNumber: line,
        startColumn: 1,
        endColumn: endCol,
      };
    });

    monaco.editor.setModelMarkers(model, "kidcode", markers);
  } catch (error) {
    if (error.name === "AbortError") return; // ignore outdated requests
    console.error("Validation request failed:", error);
  } finally {
    if (validationController === controller) validationController = null;
  }
}

// --- 5. HELPER FUNCTIONS ---
function clearCanvas() {
  ctx.clearRect(0, 0, drawingCanvas.width, drawingCanvas.height);
  ctx.setTransform(1, 0, 0, 1, 0, 0);
  ctx.strokeStyle = "black";
  ctx.lineWidth = 2;
}

// Draw the classic pointer at (x, y) with direction (degrees) and color
function drawCody(x, y, direction, color) {
  ctx.save();
  ctx.translate(x, y);
  ctx.rotate((direction * Math.PI) / 180);
  ctx.beginPath();
  ctx.moveTo(0, -18); // Tip
  ctx.lineTo(10, 7); // Bottom right
  ctx.lineTo(0, 0); // Indented base center
  ctx.lineTo(-4, 7); // Bottom left
  ctx.closePath();
  ctx.fillStyle = color;
  ctx.fill();
  ctx.strokeStyle = "black";
  ctx.lineWidth = 1.5;
  ctx.stroke();
  ctx.restore();
}

function logToOutput(message, type = "info") {
  const line = document.createElement("div");
  line.textContent = message;
  if (type === "error") {
    line.style.color = "red";
    line.style.fontWeight = "bold";
  }
  outputArea.appendChild(line);
}

// Store lines and Cody state for redraw
let drawnLines = [];
let codyState = { x: 250, y: 250, direction: 0, color: "blue" };

function renderEvents(events) {
  if (!events || events.length === 0) return;

  for (const event of events) {
    switch (event.type) {
      case "ClearEvent":
        drawnLines = [];
        codyState = { x: 250, y: 250, direction: 0, color: "blue" };
        break;
      case "MoveEvent":
        if (
          event.isPenDown &&
          (event.fromX !== event.toX || event.fromY !== event.toY)
        ) {
          drawnLines.push({
            fromX: event.fromX,
            fromY: event.fromY,
            toX: event.toX,
            toY: event.toY,
            color: event.color ?? codyState.color,
          });
        }
        codyState = {
          x: event.toX,
          y: event.toY,
          direction: event.newDirection,
          color: event.color ?? codyState.color,
        };
        break;
      case "SayEvent":
        logToOutput(`Cody says: ${event.message}`);
        break;
      case "ErrorEvent":
        logToOutput(`ERROR: ${event.errorMessage}`, "error");
        break;
    }
  }
  redrawCanvas();
}

function redrawCanvas() {
  ctx.clearRect(0, 0, drawingCanvas.width, drawingCanvas.height);
  drawnLines.forEach((line) => {
    ctx.beginPath();
    ctx.moveTo(line.fromX, line.fromY);
    ctx.lineTo(line.toX, line.toY);
    ctx.strokeStyle = line.color;
    ctx.lineWidth = 2;
    ctx.stroke();
  });
  drawCody(codyState.x, codyState.y, codyState.direction, codyState.color);
}

helpButton.addEventListener("click", () => {
  helpModal.classList.remove("hidden");
});

closeButton.addEventListener("click", () => {
  helpModal.classList.add("hidden");
});

window.addEventListener("click", (event) => {
  if (event.target === helpModal) {
    helpModal.classList.add("hidden");
  }
});
