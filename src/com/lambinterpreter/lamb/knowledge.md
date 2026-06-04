# Lexical Analysis vs Syntax Analysis

## Lexical Analysis (Scanning / Tokenizing)

**Purpose:**

- Converts characters into meaningful tokens.
- Performed by the Lexer/Scanner.

**Flow:**
Characters → Tokens

**Example:**
Source:
var age = 20;

Tokens:
VAR
IDENTIFIER(age)
EQUAL
NUMBER(20)
SEMICOLON

**Lexical Errors:**

- Invalid characters (@, #, etc. if not allowed)
- Unterminated strings
- Malformed numbers

**Question it answers:**
"What are these words?"

---

## Syntax Analysis (Parsing)

**Purpose:**

- Checks whether the token sequence follows the language grammar.
- Performed by the Parser.

**Flow:**
Tokens → Parse Tree / AST

**Example:**
Tokens:
VAR IDENTIFIER(age) EQUAL NUMBER(20) SEMICOLON

**AST:**
VarDeclaration
├── name: age
└── value: 20

**Syntax Errors:**

- Missing identifiers
- Missing operators
- Missing parentheses/braces
- Invalid statement structure

**Example:**
var = 20;

Tokens are valid, but grammar is invalid.

**Question it answers:**
"Do these words form a valid sentence?"

---

## Compiler / Interpreter Pipeline

Source Code
↓
Lexer (Lexical Analysis)
↓
Tokens
↓
Parser (Syntax Analysis)
↓
AST
↓
Interpreter / Compiler

---

**Quick Memory Trick:**

- Lexer = Characters → Tokens
- Parser = Tokens → AST

Lexer identifies words.
Parser checks sentence structure.

**Parse Tree (Concrete Syntax Tree)**

- Represents the full grammar.
- Contains every non-terminal and terminal.
- Larger and more detailed.

**Abstract Syntax Tree (AST)**

- Simplified version of the parse tree.
- Removes grammar-specific nodes.
- Keeps only semantic structure.
- Used by interpreters and compilers.

# Common Operations on an AST

An AST (Abstract Syntax Tree) can be traversed multiple times for different purposes.
Each traversal is called a pass or operation.

Source Code
↓
Lexer
↓
Tokens
↓
Parser
↓
AST
↓
Various Operations

---

1. Interpret

---

**Purpose:**

- Executes the program.
- Evaluates expressions and statements.

**Example:**
1 + 2 \* 3

**Evaluation:**
2 \* 3 = 6
1 + 6 = 7

**Result:**
7

---

2. Resolve

---

**Purpose:**

- Determines which variable declaration a name refers to.
- Performs scope analysis and name binding.

**Example:**

    var a = "global";

    {
        var a = "local";
        print a;
    }

**Resolver determines:**
print a -> local variable

Used before interpretation.

---

3. Type Checking

---

**Purpose:**

- Verifies that operations use compatible types.
- Common in statically typed languages.

**Example:**

    int x = "hello";

**Error:**
Expected int
Found String

lamb is dynamically typed, so it does not need a full type checker.

---

4. Optimization

---

**Purpose:**

- Improves performance.
- Simplifies the AST before execution.

**Example:**

    2 + 3

Before: +
/ \
 2 3

**After (Constant Folding):**
5

**Common optimizations:**

- Constant folding
- Dead code elimination
- Strength reduction

---

5. Pretty Printing

---

**Purpose:**

- Converts an AST back into a readable string.
- Useful for debugging and visualization.

**Example AST:**

      *
     / \
    -   3

/
123

**Output:**
(\* (- 123) 3)

Crafting Interpreters first implements this using AstPrinter.

---

## Why Not Put These Methods Inside AST Nodes?

Bad Design:

class Binary {
interpret()
resolve()
typeCheck()
optimize()
prettyPrint()
}

**Problem:**

- Mixes multiple responsibilities.
- Hard to maintain.
- Violates Separation of Concerns.

**Better Design (Visitor Pattern):**

Interpreter.visitBinaryExpr()
Resolver.visitBinaryExpr()
TypeChecker.visitBinaryExpr()
Optimizer.visitBinaryExpr()
AstPrinter.visitBinaryExpr()

AST nodes remain simple data structures.

---

## Quick Memory Trick

Interpret -> Run the program
Resolve -> Find variable scope/binding
TypeCheck -> Verify types
Optimize -> Improve performance
PrettyPrint -> Convert AST to readable text

Same AST, different operations.

```text
# Expression Problem & Visitor Pattern Motivation

## AST as a Table

AST Types (Rows):
    Binary
    Literal
    Unary
    Grouping

Operations (Columns):
    Interpret
    Resolve
    TypeCheck
    Optimize
    PrettyPrint

Table:

                 Interpret  Resolve  TypeCheck  Optimize  PrettyPrint
Binary              ✓          ✓         ✓          ✓          ✓
Literal             ✓          ✓         ✓          ✓          ✓
Unary               ✓          ✓         ✓          ✓          ✓
Grouping            ✓          ✓         ✓          ✓          ✓

Each cell contains a unique implementation.

Example:
    Interpret(Binary)
    Interpret(Literal)
    Resolve(Binary)
    PrettyPrint(Unary)

--------------------------------------------------
Object-Oriented Approach (Java)
--------------------------------------------------

Java organizes code by TYPES (rows).

Example:

class Binary extends Expr {
    interpret()
    resolve()
    typeCheck()
    optimize()
    prettyPrint()
}

class Literal extends Expr {
    interpret()
    resolve()
    typeCheck()
    optimize()
    prettyPrint()
}

**Advantages:**
- Easy to add new AST node types (rows).
- Just create a new class.

**Example:**
    class Call extends Expr

No existing classes need modification.

**Disadvantages:**
- Hard to add new operations (columns).
- Must modify every existing class.

**Adding:**
    optimize()

**Requires changes in:**
    Binary
    Literal
    Unary
    Grouping
    Call
    ...

**Summary:**
    OOP → Easy Rows, Hard Columns

--------------------------------------------------
Functional Approach (ML, Haskell, OCaml)
--------------------------------------------------

Functional languages organize code by OPERATIONS (columns).

Example:

interpret(expr)
resolve(expr)
typeCheck(expr)
prettyPrint(expr)

Each function handles all expression types.

interpret(expr):
    Binary  -> ...
    Literal -> ...
    Unary   -> ...

**Advantages:**
- Easy to add new operations.
- Just create another function.

**Example:**
    optimize(expr)

No existing code changes.

**Disadvantages:**
- Hard to add new types.
- Every existing function must be updated.

**Adding:**
    Call

**Requires changes in:**
    interpret()
    resolve()
    typeCheck()
    prettyPrint()
    optimize()

**Summary:**
    Functional → Easy Columns, Hard Rows

--------------------------------------------------
The Expression Problem
--------------------------------------------------

**Question:**

Can we design a system where:
    1. Adding new types is easy
    2. Adding new operations is easy

without modifying existing code?

This is called the Expression Problem.

No universally perfect solution exists.

--------------------------------------------------
Why This Matters for ASTs
--------------------------------------------------

ASTs usually gain new operations over time:

    Interpret
    Resolve
    PrettyPrint
    TypeCheck
    Optimize
    Compile
    Analyze

Much more frequently than new node types.

Therefore grouping code by operation is often more natural.

--------------------------------------------------
Visitor Pattern
--------------------------------------------------

Instead of:

Binary.interpret()
Binary.resolve()
Binary.prettyPrint()

Use:

Interpreter.visitBinaryExpr()
Resolver.visitBinaryExpr()
AstPrinter.visitBinaryExpr()

Benefits:
- All interpreter logic stays in Interpreter.
- All resolver logic stays in Resolver.
- All printing logic stays in AstPrinter.
- AST classes remain simple data structures.

Example:

class Binary extends Expr {
    Expr left;
    Token operator;
    Expr right;
}

No interpret(), resolve(), optimize(), etc.

--------------------------------------------------
Quick Memory Trick
--------------------------------------------------

OOP:
    Organizes by Types (Rows)
    Easy Types
    Hard Operations

Functional:
    Organizes by Operations (Columns)
    Easy Operations
    Hard Types

Visitor Pattern:
    Brings a column-oriented style to OOP.
    Keeps AST nodes as data.
    Groups behavior by operation.
```

```text
# Visitor Pattern - Intuition

## The Problem

Given an AST:

abstract class Expr {}

class Binary extends Expr {}
class Literal extends Expr {}
class Unary extends Expr {}

The interpreter receives:

    Expr expr

But Expr is only the parent type.

At runtime, expr could be:
    Binary
    Literal
    Unary
    Grouping
    ...

--------------------------------------------------
Without Visitor
--------------------------------------------------

Interpreter must manually identify the node type.

Example:

Object evaluate(Expr expr){
    if(expr instanceof Binary){
        ...
    }
    else if(expr instanceof Literal){
        ...
    }
    else if(expr instanceof Unary){
        ...
    }
}

Problem:
- Long instanceof chains.
- Must update interpreter whenever a new node type is added.
- Same issue appears in Resolver, AstPrinter, Optimizer, etc.

Mental Model:

Interpreter asks:
    "What are you?"
    Binary?
    Literal?
    Unary?

--------------------------------------------------
Visitor Idea
--------------------------------------------------

Instead of:

    Interpreter identifies the node.

Use:

    Node identifies itself.

The AST node tells the visitor which method to call.

--------------------------------------------------
Step 1: Add accept()
--------------------------------------------------

abstract class Expr {
    abstract void accept(Visitor visitor);
}

--------------------------------------------------
Step 2: Each Node Knows Its Own Type
--------------------------------------------------

class Binary extends Expr {
    void accept(Visitor visitor){
        visitor.visitBinary(this);
    }
}

class Literal extends Expr {
    void accept(Visitor visitor){
        visitor.visitLiteral(this);
    }
}

--------------------------------------------------
Step 3: Use It
--------------------------------------------------

Expr expr = new Binary();

Interpreter interpreter = new Interpreter();

expr.accept(interpreter);

--------------------------------------------------
What Happens?
--------------------------------------------------

1. Runtime sees:
       expr contains Binary

2. Calls:
       Binary.accept(interpreter)

3. Binary says:
       visitor.visitBinary(this)

4. Interpreter executes:
       visitBinary()

Result:
- No instanceof checks.
- Correct method selected automatically.

--------------------------------------------------
Mental Model
--------------------------------------------------

Without Visitor:

Interpreter
    ↓
Is Binary?
    ↓
Is Literal?
    ↓
Is Unary?

With Visitor:

Interpreter
    ↓
expr.accept(this)

Binary
    ↓
visitBinary()

Literal
    ↓
visitLiteral()

Unary
    ↓
visitUnary()

--------------------------------------------------
Why This Works
--------------------------------------------------

The node itself knows its real type.

Binary knows:
    "I am Binary."

Literal knows:
    "I am Literal."

Unary knows:
    "I am Unary."

So each node routes execution to the correct visitor method.

--------------------------------------------------
Double Dispatch
--------------------------------------------------

Normal OOP:

expr.method()

One runtime decision:
    Which Expr subclass?

Visitor:

expr.accept(visitor)

Decision #1:
    Which Expr subclass?

Inside accept():

visitor.visitBinary(this)

Decision #2:
    Which Visitor implementation?

Therefore:
    Visitor = Double Dispatch

--------------------------------------------------
Goal of Visitor Pattern
--------------------------------------------------

Move operations out of AST classes while avoiding:

    if(expr instanceof Binary)
    else if(expr instanceof Literal)
    else if(expr instanceof Unary)

AST remains mostly data.

Operations live in separate classes:

    Interpreter
    Resolver
    AstPrinter
    Optimizer
    TypeChecker

Each operation gets its own visitor.
```

# Parsing Ambiguity, Precedence & Recursive Descent

## Why the Original Grammar Was Bad

Old grammar:

```text
expression → literal
           | unary
           | binary

binary → expression operator expression
```

Problem:

```lamb
6 / 3 - 1
```

can be parsed as:

```lamb
(6 / 3) - 1
```

or

```lamb
6 / (3 - 1)
```

Both are valid according to the grammar.

➡️ Same source code, different ASTs, different results.

**This is called grammar ambiguity.**

---

## Ambiguity

A grammar is ambiguous when a single token sequence can generate multiple valid syntax trees.

Example:

```lamb
6 / 3 - 1
```

Possible ASTs:

```text
(6 / 3) - 1
```

```text
6 / (3 - 1)
```

Ambiguous grammars are bad because the parser cannot determine the programmer's intent.

---

## Precedence

**Precedence determines which operator binds tighter.**

Example:

```lamb
6 / 3 - 1
```

Since:

```text
* /
```

have higher precedence than:

```text
+ -
```

it becomes:

```lamb
(6 / 3) - 1
```

### Precedence Table (Low → High)

| Level | Operators |
|---------|------------|
| Equality | `== !=` |
| Comparison | `> >= < <=` |
| Term | `+ -` |
| Factor | `* /` |
| Unary | `! -` |

---

## Associativity

Determines evaluation order among operators of the same precedence.

### Left Associative

```lamb
5 - 3 - 1
```

means:

```lamb
(5 - 3) - 1
```

Used by:

```text
+ - * / == != > < >= <=
```

### Right Associative

```lamb
a = b = c
```

means:

```lamb
a = (b = c)
```

Used by assignment.

---

## Fixing Ambiguity

Instead of:

```text
expression
```

handling everything,

create one rule per precedence level:

```text
expression
equality
comparison
term
factor
unary
primary
```

Hierarchy:

```text
expression
    ↓
equality
    ↓
comparison
    ↓
term
    ↓
factor
    ↓
unary
    ↓
primary
```

Lower levels can contain higher levels.

---

## Expression Rule

```text
expression → equality
```

Expression simply delegates to the lowest precedence rule.

Reason:

- Easier to extend later.
- Keeps grammar readable.

---

## Primary Expressions

Highest-precedence expressions.

```text
primary →
      NUMBER
    | STRING
    | true
    | false
    | nil
    | "(" expression ")"
```

Examples:

```lamb
123
"hello"
true
(1 + 2)
```

---

## Unary Expressions

First attempt:

```text
unary →
    ("!" | "-") unary
```

Problem:

```lamb
123
```

cannot be parsed.

The rule never terminates.

Fixed version:

```text
unary →
      ("!" | "-") unary
    | primary
```

Examples:

```lamb
-5
!true
!!true
```

Recursion eventually stops at `primary`.

---

## Left Recursion

Bad rule:

```text
factor →
      factor ("*" | "/") unary
    | unary
```

Notice:

```text
factor → factor ...
```

Parser implementation would become:

```java
factor() {
    factor();
}
```

Result:

```text
factor()
  factor()
    factor()
      factor()
      ...
```

Infinite recursion.

This is called **left recursion**.

---

## Eliminating Left Recursion

Replace:

```text
factor →
      factor ("*" | "/") unary
    | unary
```

with:

```text
factor →
      unary (("*" | "/") unary)*
```

Meaning:

```text
one unary

followed by

zero or more:
    operator + unary
```

Examples:

```lamb
1
1 * 2
1 * 2 / 3
1 * 2 / 3 * 4
```

No infinite recursion.

---

## Final Expression Grammar

```text
expression → equality ;

equality →
    comparison (("!=" | "==") comparison)* ;

comparison →
    term ((">" | ">=" | "<" | "<=") term)* ;

term →
    factor (("-" | "+") factor)* ;

factor →
    unary (("/" | "*") unary)* ;

unary →
      ("!" | "-") unary
    | primary ;

primary →
      NUMBER
    | STRING
    | "true"
    | "false"
    | "nil"
    | "(" expression ")" ;
```

---

## Biggest Takeaways

### Scanner

```text
Source Code
    ↓
Tokens
```

### Parser

```text
Tokens
    ↓
AST
```

### Parser's Job

```text
Determine structure
Resolve precedence
Resolve associativity
Build AST
```

### Recursive Descent Parser Trick

Every grammar rule becomes a method:

```java
expression()
equality()
comparison()
term()
factor()
unary()
primary()
```

This grammar is literally the blueprint for the parser code you'll write next.

---

## Quick Revision

```text
Ambiguous Grammar
    ↓
Need Precedence

Precedence
    ↓
Need Multiple Rules

Multiple Rules
    ↓
Recursive Descent Methods

Methods
    ↓
Build AST

AST
    ↓
Interpreter Executes
```

# Recursive Descent Parsing

## Why Recursive Descent?

Many parsing techniques exist:

```text
LL(k), LR(1), LALR
Earley
Packrat
Parser Combinators
Shunting Yard
```

For lamb, Recursive Descent is sufficient because it is:

- Simple
- Handwritten
- Fast
- Robust
- Supports good error handling

Used in real-world projects:

- GCC
- V8 (JavaScript engine)
- Roslyn (C# compiler)

---

## Top-Down vs Bottom-Up

### Top-Down (Recursive Descent)

Starts from the outermost rule:

```text
expression
    ↓
equality
    ↓
comparison
    ↓
term
    ↓
factor
    ↓
unary
    ↓
primary
```

Builds the tree from root to leaves.

### Bottom-Up (LR Parsers)

Starts from small pieces:

```text
1
+
2
*
3
```

Gradually combines them into larger expressions until the full AST is built.

---

## Why "Recursive" Descent?

Grammar:

```text
unary →
    ("!" | "-") unary
    | primary
```

contains:

```text
unary → unary
```

which becomes:

```java
Expr unary() {
    if(match(BANG, MINUS)) {
        return new Expr.Unary(previous(), unary());
    }

    return primary();
}
```

A rule calling itself in the grammar becomes a recursive function call.

---

## Grammar → Code Translation

| Grammar | Code |
|----------|------|
| Terminal | Match/consume token |
| Nonterminal | Function call |
| `\|` | `if` / `switch` |
| `*` | `while` loop |
| `+` | `for` / `while` loop |
| `?` | `if` statement |

Examples:

### Terminal

```text
NUMBER
```

↓

```java
match(NUMBER);
```

### Nonterminal

```text
term
```

↓

```java
term();
```

### Choice

```text
NUMBER | STRING
```

↓

```java
if(match(NUMBER)) ...
else if(match(STRING)) ...
```

### Repetition

```text
("*" unary)*
```

↓

```java
while(match(STAR)) {
    ...
}
```

### Optional

```text
something?
```

↓

```java
if(...) {
    ...
}
```

---

## Biggest Takeaway

Recursive Descent is a direct translation of grammar into code:

```text
Grammar Rule
      ↓
Java Method

Nonterminal
      ↓
Method Call

Grammar Recursion
      ↓
Function Recursion
```

Parser structure:

```java
expression()
equality()
comparison()
term()
factor()
unary()
primary()
```

Each grammar rule becomes one parser method.

# Parser Error Handling & Synchronization

## Parser Responsibilities

```text
Valid Tokens
    ↓
Build AST

Invalid Tokens
    ↓
Report Errors
```

A good parser should:

- Detect errors
- Avoid crashing
- Report multiple errors
- Minimize cascaded (fake) errors

---

## ParseError Sentinel Class

```java
private static class ParseError extends RuntimeException {}
```

Purpose:

- Special exception type
- Carries no data
- Used only to unwind the parser

Think:

```text
ParseError
    ↓
"Abort current parse branch"
```

---

## Why `return new ParseError()`?

```java
private ParseError error(Token token, String message) {
    lamb.error(token, message);
    return new ParseError();
}
```

Allows caller to choose:

### Report Only

```java
error(token, message);
```

```text
Report Error
Continue Parsing
```

### Report + Abort

```java
throw error(token, message);
```

```text
Report Error
Unwind Parser
```

---

## Parser State

Recursive descent parser state is stored in the Java call stack.

Example:

```text
expression()
comparison()
term()
factor()
unary()
primary()
```

No explicit:

```java
currentRule = ...
```

The call stack itself is the parser state.

---

## Stack Unwinding

Error found:

```text
expression()
comparison()
term()
factor()
unary()
primary() ← ERROR
```

```java
throw new ParseError();
```

Java automatically exits:

```text
primary()
unary()
factor()
term()
comparison()
expression()
```

until it reaches:

```java
catch(ParseError e)
```

This is called:

```text
Stack Unwinding
```

---

## Error Recovery

Goal:

```text
Find Error
    ↓
Recover
    ↓
Continue Parsing
```

Without recovery:

```text
1 Error
Stop
```

With recovery:

```text
Error 1
Error 2
Error 3
```

---

## Synchronization

Definition:

```text
Synchronization =
Skipping tokens until a safe point
where parsing can resume.
```

Example:

```lamb
var x = + * /
print "hello";
```

Skip:

```text
+
*
/
```

Resume at:

```lamb
print
```

---

## Synchronization Points

### Semicolon

```lamb
var x = ;
print "hello";
```

After:

```text
;
```

we are probably done with the bad statement.

### Statement Keywords

```text
class
fun
var
for
if
while
print
return
```

These usually mark the start of a new statement.

---

## Full Error Recovery Flow

```text
Syntax Error
      ↓
throw ParseError
      ↓
Stack Unwinding
      ↓
catch(ParseError)
      ↓
Parser State Reset
      ↓
synchronize()
      ↓
Skip Bad Tokens
      ↓
Resume Parsing
```

## Quick Revision

```text
ParseError
    ↓
Reset Parser State

synchronize()
    ↓
Reset Token Position

Both Together
    ↓
Error Recovery
    ↓
Continue Parsing
```
# Representing lamb Values in Java

## Problem
- lamb is **dynamically typed**:
  ```lamb
  var x = 10;
  x = "hello";
  x = true;
  ```
- Java is **statically typed**, so a variable normally has one fixed type.

## Solution
Use Java's `Object` type to represent any lamb value:

```java
Object value;
```

`Object` can hold numbers, strings, booleans, and `null`.

## lamb → Java Mapping

| lamb Type | Java Representation |
|-----------|--------------------|
| Any value | `Object` |
| nil | `null` |
| Boolean | `Boolean` |
| number | `Double` |
| string | `String` |

## Runtime Type Checking

The interpreter uses `instanceof` to determine the actual type:

```java
if (value instanceof Double) {
    // number
}

if (value instanceof String) {
    // string
}
```

Example:
- `1 + 2` → numeric addition (`Double`)
- `"a" + "b"` → string concatenation (`String`)

## Why It Works
- Java's boxed types (`Double`, `Boolean`, etc.) are subclasses of `Object`.
- The JVM already stores runtime type information.
- jlamb can reuse Java's object system instead of building its own value representation.

## Key Idea
**Every lamb value is stored as a Java `Object`, and the interpreter uses `instanceof` to determine its actual runtime type.**


# Statements, Expressions, Side Effects, and Interpreter State

## Expression

An expression is code that computes and produces a value.

Examples:

```lox
1 + 2
x * 5
"Dee" + "Jha"
```

Mental model:

> Expression = "What is this value?"

---

## Statement

A statement is a command executed by the interpreter.

Examples:

```lox
print 1 + 2;
var x = 10;
x = 20;
```

Mental model:

> Statement = "Do this."

Statements are executed for their actions, not for producing values.

---

## Expression Statement

An expression can be used as a statement by adding a semicolon.

```lox
1 + 2;
```

The expression is evaluated and the resulting value is discarded.

Grammar:

```text
exprStmt → expression ";" ;
```

---

## Side Effect

A side effect is any observable change caused during execution other than simply producing a value.

### No Side Effect

```lox
1 + 2
```

Produces:

```text
3
```

Only computes a value.

### Side Effects

```lox
print 3;
```

Output appears on the screen.

```lox
var x = 10;
```

Interpreter memory changes.

```lox
x = 20;
```

Stored value changes.

### Rule

> If execution changes the interpreter's state or the outside world, it is a side effect.

---

## Interpreter State

Interpreter state is all the information the interpreter remembers while running a program.

Example:

```lox
var x = 10;
print x;
```

After the first statement, the interpreter remembers:

```text
x -> 10
```

Without this remembered information, the second statement would not work.

### State Evolution

```lox
var x = 10;
x = 20;
```

```text
Start:
{}

After var x = 10:
{x -> 10}

After x = 20:
{x -> 20}
```

Mental model:

> The interpreter has a notebook where it stores variable names and their values.

---

## Relationship

```text
Statement
    ↓ executes
Interpreter performs action
    ↓
State changes / output appears
    ↓
Side Effect
```

Examples:

```lox
var x = 10;
```

Side effect:

```text
x -> 10
```

```lox
print x;
```

Side effect:

```text
Output displayed on screen
```

---

## Quick Summary

- Expression → computes a value.
- Statement → performs an action.
- Expression Statement → expression used as a statement.
- Side Effect → observable change caused during execution.
- Interpreter State → information remembered by the interpreter.
- Variables exist because the interpreter maintains state.
- Most useful statements create side effects.


# Nested Scopes and Environment Chaining

## Problem with a Single Environment

Using one global environment works until blocks are introduced.

Example:

```lox
var volume = 100;

{
    var volume = 50;
    print volume;
}

print volume;
```

Expected Output:

```text
50
100
```

With a single environment:

```text
{ volume -> 100 }

↓ declare inner volume

{ volume -> 50 }
```

The outer variable is overwritten and lost.

---

## Shadowing

When a local variable has the same name as a variable in an enclosing scope, the local variable **shadows** the outer one.

Example:

```lox
var x = "global";

{
    var x = "local";
    print x;
}

print x;
```

Output:

```text
local
global
```

Inside the block, the local `x` hides the global `x`.

---

## Solution: One Environment per Scope

Instead of one giant environment:

```text
Environment
{
    global -> "outside"
    local  -> "inside"
}
```

Create a new environment for every scope.

```text
Global Environment
{
    global -> "outside"
}

Block Environment
{
    local -> "inside"
}
```

---

## Environment Chaining

Each environment stores a reference to its enclosing environment.

```java
class Environment {
    final Environment enclosing;
}
```

Visualized:

```text
Block Environment
{
    local -> "inside"
}
      |
      v
Global Environment
{
    global -> "outside"
}
```

---

## Variable Lookup

Example:

```lox
var global = "outside";

{
    var local = "inside";
    print global + local;
}
```

Lookup Process:

```text
Need local
↓
Found in current environment

Need global
↓
Not found in current environment
↓
Check enclosing environment
↓
Found
```

---

## Lookup Algorithm

```java
Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
        return values.get(name.lexeme);
    }

    if (enclosing != null) {
        return enclosing.get(name);
    }

    throw new RuntimeError(...);
}
```

Search starts from the innermost scope and moves outward.

---

## Why Shadowing Works

Example:

```lox
var x = "global";

{
    var x = "local";
    print x;
}
```

Lookup starts in:

```text
Block Environment
{
    x -> "local"
}
```

Variable found immediately.

The global environment is never checked.

Thus the local variable shadows the outer one automatically.

---

## Entering a Block

Before:

```text
currentEnvironment
      ↓
Global
```

Enter block:

```java
environment =
    new Environment(environment);
```

After:

```text
currentEnvironment
      ↓
Block
      ↓
Global
```

---

## Exiting a Block

Leave block:

```java
environment =
    environment.enclosing;
```

After:

```text
currentEnvironment
      ↓
Global
```

The block environment becomes unreachable and can be garbage collected.

---

## Cactus Stack

Over the lifetime of a program:

```text
Global
  |
  +-- Block A
  |      |
  |      +-- Block B
  |
  +-- Block C
```

Only one path is active at a time.

This structure is called a **Cactus Stack** (or parent-pointer tree).

---

## Key Takeaways

- One scope = One Environment.
- Each Environment points to its enclosing Environment.
- Variable lookup starts from the current scope and walks outward.
- Local variables shadow outer variables.
- Entering a block creates a new Environment.
- Exiting a block restores the previous Environment.
- Environment chaining is the foundation for lexical scoping.

# Functions & Native Functions (Crafting Interpreters)

## Why Functions?

Functions let us:
- Reuse code
- Organize logic
- Call code from multiple places
- Support recursion

---

## Function Call Flow

```lox
add(1, 2);
```

1. Evaluate callee (`add`)
2. Evaluate arguments (`1`, `2`)
3. Check callee is callable
4. Check argument count (arity)
5. Execute function body
6. Return result

---

## LoxCallable Interface

All callable objects implement:

```java
interface LoxCallable {
    int arity();
    Object call(Interpreter interpreter,
                List<Object> arguments);
}
```

### Purpose

- `arity()` → number of expected arguments
- `call()` → executes the function

This allows the interpreter to treat:
- User-defined functions
- Native functions
- Classes (later)

uniformly.

---

## Evaluating Call Expressions

```java
visitCallExpr()
```

Steps:

1. Evaluate callee
2. Evaluate arguments
3. Verify callee is a `LoxCallable`
4. Verify arity matches
5. Invoke:

```java
function.call(this, arguments);
```

---

## Native Functions

### Definition

Functions callable from Lox but implemented in Java.

Example:

```lox
clock();
```

Implementation exists in Java, not Lox.

---

## Why Native Functions?

Some features require OS/runtime access:

- Time
- Files
- Networking
- Random numbers
- Graphics

These cannot be implemented purely in Lox.

---

## clock()

Added as first native function.

```lox
var start = clock();

/* work */

print clock() - start;
```

Used for benchmarking.

---

## Registering Native Functions

Inside Interpreter constructor:

```java
globals.define("clock", ...);
```

This inserts `clock` into the global environment.

So users can call:

```lox
print clock();
```

without defining it themselves.

---

## FFI (Foreign Function Interface)

FFI = system allowing a language to call code written in another language.

Examples:

- Python → C
- Java → C/C++ (JNI)
- JavaScript → C++ (Node APIs)

### Difference

Native Function:
- Single built-in foreign function.

FFI:
- Full mechanism for users to create/import foreign functions.

jlox:
- Has native functions.
- Does NOT implement a full FFI.

---

## Key Idea

Every callable thing in Lox is represented by:

```java
LoxCallable
```

and is executed through:

```java
call(...)
```

Whether it's:
- a native function,
- a user-defined function,
- or later, a class constructor.


# LoxCallable Notes

## What is LoxCallable?

`LoxCallable` is an interface representing anything that can be called with `()` in Lox.

```java
interface LoxCallable {
    int arity();
    Object call(Interpreter interpreter,
                List<Object> arguments);
}
```

Examples:
- Native functions (`clock`)
- User-defined functions (`fun greet() {}`)

---

## Native Function Example

```java
globals.define("clock", new LoxCallable() {
    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter,
                       List<Object> arguments) {
        return (double) System.currentTimeMillis() / 1000.0;
    }
});
```

This creates an anonymous object implementing `LoxCallable`.

Environment:

```text
"clock" -> LoxCallable object
```

---

## How `clock()` Executes

Lox code:

```lox
clock();
```

AST:

```text
Call
├── callee: Variable(clock)
└── arguments: []
```

Interpreter:

```java
Object callee = evaluate(expr.callee);
```

`expr.callee` is the variable `clock`.

Evaluating it:

```java
environment.get("clock")
```

returns:

```text
LoxCallable object
```

---

## instanceof Check

```java
if (!(callee instanceof LoxCallable)) {
    throw new RuntimeError(...);
}
```

Meaning:

> If the evaluated value is NOT callable, throw an error.

Examples:

```lox
clock();   // valid
```

```text
callee -> LoxCallable object
```

```lox
123();     // invalid
```

```text
callee -> Double
```

---

## First-Class Functions

Functions are stored as values just like numbers and strings.

```text
globals
├── x     -> 10
├── name  -> "Dee"
└── clock -> LoxCallable object
```

Evaluating a variable returns its stored value.

```lox
x
```

returns:

```text
10
```

```lox
clock
```

returns:

```text
LoxCallable object
```

Then `()` invokes its `call()` method.