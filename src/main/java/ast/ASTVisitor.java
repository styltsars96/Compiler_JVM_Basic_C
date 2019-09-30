package ast;

import ast.exprs.*;
import ast.statements.*;
import ast.statements.assign.ArrayIndexAssignment;
import ast.statements.assign.ArrayInitAssignment;
import ast.statements.assign.BasicAssignment;
import ast.statements.assign.CompoundAssign;

/**
 * Abstract syntax tree visitor.
 */
public interface ASTVisitor {

    
    void visit(CompUnit node) throws ASTVisitorException;

    //Declarations
    void visit(VariableDeclaration node) throws ASTVisitorException;
    
    void visit(ParameterDeclaration node) throws ASTVisitorException;

    void visit(FunctionDeclaration node) throws ASTVisitorException;
    
    //Statements
    void visit(PrintStatement node) throws ASTVisitorException;
    
    void visit(IfStatement node) throws ASTVisitorException;

    void visit(IfElseStatement node) throws ASTVisitorException;
    
    void visit(WhileStatement node) throws ASTVisitorException;

    void visit(DoWhileStatement node) throws ASTVisitorException;
    
    void visit(FunctionCallStatement node) throws ASTVisitorException;
    
    void visit(ExitStatement node) throws ASTVisitorException;
    
    void visit(CompoundStatement node) throws ASTVisitorException;

    //Expressions
    void visit(IdentifierExpression node) throws ASTVisitorException;

    void visit(DoubleLiteralExpression node) throws ASTVisitorException;

    void visit(IntegerLiteralExpression node) throws ASTVisitorException;

    void visit(CharacterLiteralExpression node) throws ASTVisitorException;
    
    void visit(StringLiteralExpression node) throws ASTVisitorException;
    
    void visit(BinaryExpression node) throws ASTVisitorException;

    void visit(UnaryExpression node) throws ASTVisitorException;

    void visit(ParenthesisExpression node) throws ASTVisitorException;

    void visit(FunctionCallExpression node) throws ASTVisitorException;

    //Assignments
    void visit(BasicAssignment node) throws ASTVisitorException;

    void visit(ArrayInitAssignment node) throws ASTVisitorException;
    
    void visit(ArrayIndexAssignment node) throws ASTVisitorException;
    
    void visit(CompoundAssign node) throws ASTVisitorException;
}
