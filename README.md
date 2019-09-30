# Compiler.
A basic custom compiler for a subset of C, for the JVM. Exercise for Compilers Course @HUA 2018, written in Java.
* Lexical Analysis (Lexer) is made with JFlex.
* LALR(1) Syntax Analyzer / Parser made with CUP.
* Abstract Syntax Tree parsed using the Visitor pattern.
* Semantic Analyzer creates symbol table, and data types are based on ASM.
* (An extra visitor that creates 3-address code, that corresponds to most forms of machine code or bytecode as an intermediate test)
* JVM bytecode generation using ASM.
