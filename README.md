# JavaFX Paint Program (MVC)

A JavaFX paint application that lets you draw and edit shapes on a canvas, change styling (color/line width/fill), select and move shapes (single or multi-select), and use **Cut / Copy / Paste / Delete** with **Undo / Redo**. It also supports **Import Image** (adds an image as a drawable item on the canvas).

> Note: This README documents only the **Paint** code you uploaded (it intentionally excludes the Othello files).

---

## Tech stack and GUI

### Java / JavaFX features used
- **JavaFX Application lifecycle**: `Paint` extends `javafx.application.Application` and launches the UI via `start(Stage stage)`.
- **Scene Graph UI**: `View` builds the UI using `Scene`, `Stage`, and layouts like `BorderPane` + `StackPane`.
- **Canvas rendering**: `PaintPanel` extends `Canvas` and draws using `GraphicsContext` (`fillOval`, `strokeRect`, etc.).
- **Controls**:
  - `MenuBar`, `Menu`, `MenuItem` for File/Edit actions.
  - `ColorPicker` to set stroke/fill color for new shapes and update selected shapes.
  - `Slider` for line width.
  - Buttons for shape selection (Select/Circle/Rectangle/…).
  - `Alert` for error dialogs.
- **Event handling**:
  - Menu actions via `EventHandler<ActionEvent>` in `View`.
  - Mouse drawing and selection via `EventHandler<MouseEvent>` in `PaintPanel` + per-shape `DrawingStrategy`.
  - Keyboard shortcuts in `PaintPanel` (`ESC` clears selection, `DELETE` deletes selection).
- **File & image import**: `FileChooser` + `javafx.scene.image.Image` for importing an image and wrapping it as a drawable (`ImageS`).

---

## High-level architecture

This project follows an **MVC-style architecture**:

- **Model**: `PaintModel`
  - Stores drawn shapes (`ArrayList<Drawable>`)
  - Stores current in-progress shape preview
  - Stores current selection
  - Owns undo/redo history (`CommandManager`)
  - Notifies observers when state changes

- **View**: `View`, `ShapeChooserPanel`
  - Builds menus and controls
  - Sends user intent to the model (e.g., undo/redo, cut/copy/paste, delete)
  - Chooses the current `DrawingStrategy` (tool)

- **Canvas View (Observer)**: `PaintPanel`
  - Observes `PaintModel`
  - Re-renders whenever the model changes
  - Delegates mouse events to the current `DrawingStrategy`

---

## Project structure and file guide

### Entry point
- `Paint.java`
  - JavaFX `Application` entry point.
  - Creates `PaintModel` and the `View`.

### app (MVC + UI wiring)
- `View.java`
  - Builds the menu bar (File/Edit), wires menu actions to model methods.
  - Handles “Import Image”.
- `PaintPanel.java`
  - JavaFX `Canvas` that observes the model and redraws shapes.
  - Sends mouse events to the active drawing strategy.
  - Keyboard shortcuts (ESC, DELETE).
- `ShapeChooserPanel.java`
  - Tool buttons + color picker + line-width slider + fill style toggle.
  - Creates and sets the current `DrawingStrategy` in `View`.
- `PaintModel.java`
  - Stores shapes, selection, current preview shape.
  - Provides operations for selection, styling, and command-based actions (undo/redo/cut/paste/move/delete).
- `FillStyle.java`
  - Enum-like type for `FILLED` vs `OUTLINE`.
- `Clipboard.java`
  - Singleton clipboard storing copied shapes (via cloning) for paste operations.

### shapes (renderable objects)
- `Drawable.java`
  - Interface all shapes implement (`draw`, `clone`, `offset`, `contains`, `getBounds`, style setters).
- Shape classes:
  - `Circle.java`, `Rectangle.java`, `Square.java`, `Oval.java`, `Triangle.java`
  - Freehand / multi-point:
    - `Squiggle.java`, `Polyline.java`
  - Utility/value objects:
    - `Point.java`
  - Image support:
    - `ImageS.java` (image drawable)
- `ShapeFactory.java`
  - Centralized shape creation based on a `type` string and points.

### strategy (drawing tools)
- `DrawingStrategy.java`
  - Interface for interpreting mouse events to create/edit shapes.
- Implementations:
  - `SelectionStrategy.java`
  - `CircleStrategy.java`
  - `RectangleStrategy.java`
  - `SquareStrategy.java`
  - `TriangleStrategy.java`
  - `OvalStrategy.java`
  - `SquiggleStrategy.java`
  - `PolylineStrategy.java`

### command.pattern (undo/redo + edit operations)
- `Command.java`
  - Interface with `execute()` and `undo()`.
- `CommandManager.java`
  - Manages undo/redo stacks.
- Concrete commands:
  - `AddCommand.java` (add shape)
  - `Delete.java`
  - `Cut.java`
  - `Paste.java`
  - `Move.java`
  - `Copy.java` (copies to clipboard; undo is intentionally no-op)

---

## Design patterns used (H / W / A / D)

### 1) MVC (Model–View–Controller style)
**H — Helps:** Separate application state (Model), UI (View), and input handling (Controller logic) so the app stays organized as features grow.  
**W — When:** When building interactive applications with many UI actions that update shared state.  
**A — Advantages:** Cleaner structure, easier debugging, easier to change UI without rewriting core logic, easier to test the model.  
**D — Disadvantages:** More files/boilerplate; boundaries can blur (e.g., some controller logic lives in view classes).

**Where in this code:** `PaintModel` (Model), `View`/`ShapeChooserPanel` (View + some Controller), `PaintPanel` (View/Canvas).

---

### 2) Observer
**H — Helps:** Automatically update the canvas when the model changes (no manual refresh calls scattered everywhere).  
**W — When:** When many UI components depend on shared state and should refresh on changes.  
**A — Advantages:** Decouples state changes from rendering; centralized updates; simpler UI wiring.  
**D — Disadvantages:** Can be harder to trace update chains; Java’s `Observable` is deprecated (a custom observer or property bindings can be a cleaner modern approach).

**Where in this code:** `PaintModel extends Observable`; `PaintPanel implements Observer` and redraws in `update()`.

---

### 3) Strategy
**H — Helps:** Switch drawing behavior (Select vs Circle vs Rectangle vs Polyline…) without rewriting `PaintPanel`.  
**W — When:** You have multiple interchangeable behaviors triggered by the same UI events (mouse events) and want to swap them at runtime.  
**A — Advantages:** Easy to add new tools; isolates tool logic; avoids huge if/else blocks.  
**D — Disadvantages:** More classes; can duplicate logic between strategies if not refactored carefully.

**Where in this code:** `DrawingStrategy` + the `*Strategy` classes. `ShapeChooserPanel` selects which strategy is active.

---

### 4) Factory (Simple Factory / Factory Method style)
**H — Helps:** Centralize shape creation so strategies don’t need to know every constructor detail for every shape type.  
**W — When:** Many places create objects with different configurations and you want one “creation gateway.”  
**A — Advantages:** Reduces duplication; keeps constructors/creation consistent; easier to modify creation rules.  
**D — Disadvantages:** A string-based factory can become a long `if/else`; adding a new shape may require modifying the factory (unless you refactor to registration).

**Where in this code:** `ShapeFactory.createShape(...)`.

---

### 5) Command (Undo/Redo)
**H — Helps:** Turn actions like Add/Delete/Move/Paste into objects that can be executed and undone.  
**W — When:** You need undo/redo, macro recording, or a clean separation between “invoker” and “operation.”  
**A — Advantages:** Undo/redo is straightforward; actions become composable; cleaner model/view interaction.  
**D — Disadvantages:** Extra classes; each new undoable action needs careful state capture.

**Where in this code:** `Command`, `CommandManager`, `AddCommand`, `Move`, `Delete`, `Cut`, `Paste`.

---

### 6) Singleton (Clipboard)
**H — Helps:** Ensure there is exactly one clipboard shared across copy/paste actions.  
**W — When:** A single shared resource is needed and global-ish access is acceptable (like a clipboard).  
**A — Advantages:** Simple access point; guarantees one instance.  
**D — Disadvantages:** Global state can make testing harder; can hide dependencies.

**Where in this code:** `Clipboard.getInstance()`.

---

### 7) Prototype (Cloning shapes)
**H — Helps:** Copy/paste duplicates shapes without depending on the exact concrete class at the call site.  
**W — When:** You need to duplicate objects that have many concrete subclasses.  
**A — Advantages:** Copy logic stays inside the shape class; clipboard can treat everything as `Drawable`.  
**D — Disadvantages:** Each shape must implement `clone()` correctly; deep-copy bugs are possible if shapes contain nested mutable state.

**Where in this code:** `Drawable.clone()` and each shape’s `clone()` implementation; `Clipboard.copy()`/`paste()`.

---

## SOLID principles in this implementation

### S — Single Responsibility Principle (SRP)
- Shapes focus on geometry + drawing (`Circle`, `Rectangle`, etc.).
- Strategies focus on interpreting mouse events into model updates (`*Strategy` classes).
- Commands focus on one reversible operation (`Delete`, `Move`, `Paste`, etc.).
- `PaintModel` stores state and exposes state-changing operations.
- `PaintPanel` handles rendering only.

### O — Open/Closed Principle (OCP)
- New tools can be added by creating a new `DrawingStrategy` without changing `PaintPanel`.
- New undoable actions can be added by creating a new `Command` and letting `CommandManager` manage it.

> Trade-off: Adding a brand-new shape currently also requires updating `ShapeFactory` and `ShapeChooserPanel` (still manageable, but not “fully closed” without a registration-based factory).

### L — Liskov Substitution Principle (LSP)
- Anywhere a `Drawable` is expected, any shape (`Circle`, `Square`, `ImageS`, etc.) can be used and still supports drawing, hit-testing (`contains`), bounds, and cloning.

### I — Interface Segregation Principle (ISP)
- `Drawable` provides the operations needed for drawing + editing + selection.
- This keeps the system consistent, but it’s a slightly “wide” interface (some shapes may not naturally need every method). A future refinement could split into smaller interfaces like `Renderable`, `Selectable`, `Stylable`, `CloneableShape`.

### D — Dependency Inversion Principle (DIP)
- High-level flow works through abstractions:
  - `CommandManager` depends on `Command` interface.
  - Rendering works through `Drawable`.
  - Tool logic works through `DrawingStrategy`.
- The UI still instantiates concrete strategies directly (`new CircleStrategy()`), which is normal for small projects; if you wanted stricter DIP, you could inject a registry of tools or use a factory for strategies too.

---

## Use cases (how to use the app)

### 1) Draw shapes
1. Click a tool button (Circle / Rectangle / Square / Triangle / Oval / Squiggle / Polyline).
2. Click and drag on the canvas to preview the shape.
3. Release the mouse to finalize and add the shape.

### 2) Select and move
- Click **Select**.
- Click a shape to select it.
- Drag the selected shape(s) to move them.
- On release, the move becomes an undoable `Move` command.

### 3) Multi-select (selection box)
- Click **Select**.
- Click on empty canvas and drag to draw a selection rectangle.
- Release to select all shapes intersecting that rectangle.

### 4) Change style of shapes
- **ColorPicker**: changes the drawing color and updates currently selected shapes’ color.
- **Line width slider**: changes line width for new shapes and updates selected shapes.
- **Fill style toggle**: switches between `FILLED` and `OUTLINE` for new shapes and updates selected shapes.

### 5) Clipboard editing
- Select one or more shapes
- Use menu: **Edit → Copy** or **Edit → Cut**
- Use menu: **Edit → Paste**
  - Paste clones the shapes and offsets them each time (so they don’t overlap exactly).

### 6) Delete
- Select shapes
- Use menu: **Edit → Delete** or press the **Delete** key.

### 7) Undo/Redo
- Use menu: **Edit → Undo** / **Edit → Redo**
- Undo/redo works for command-based actions (Add, Cut, Delete, Paste, Move).

### 8) Import Image
- Menu: **File → Import Image**
- Choose an image file; it is added as a drawable `ImageS` element (scaled if needed).

---

## Building / running (general)
This is a JavaFX app. To run it, you need a JDK and JavaFX configured (module path / VM args depend on your environment).

Typical entry point:
- `ca.utoronto.utm.assignment2.paint.Paint`

---

## Notes / future improvements (optional)
- Replace `java.util.Observable` (deprecated) with:
  - JavaFX properties/bindings, or
  - a custom observer interface, or
  - `javafx.beans` listeners.
- Refactor `ShapeFactory` to a registration-based factory to reduce long `if/else`.
- Consider splitting `Drawable` into smaller interfaces (ISP refinement).
- Add persistence (Save/Open) if needed.
