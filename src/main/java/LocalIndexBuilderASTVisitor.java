import ast.*;
import ast.exprs.*;
import ast.statements.*;
import ast.statements.assign.ArrayIndexAssignment;
import ast.statements.assign.ArrayInitAssignment;
import ast.statements.assign.BasicAssignment;
import ast.statements.assign.CompoundAssign;
import org.objectweb.asm.Type;
import symbol.HashSymTable;
import symbol.LocalIndexPool;
import symbol.SymTable;
import symbol.SymTableEntry;
import types.TypeUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class LocalIndexBuilderASTVisitor implements ASTVisitor {

    private final Deque<LocalIndexPool> env;

    public LocalIndexBuilderASTVisitor() {
        env = new ArrayDeque<LocalIndexPool>();
    }


    @Override
    public void visit(CompUnit node) throws ASTVisitorException {
        env.push(new LocalIndexPool());
        ASTUtils.setLocalIndexPool(node, env.element());
        for (Declaration d : node.getDeclarations()) d.accept(this);
        env.pop();
    }

    //Declarations
    @Override
    public void visit(VariableDeclaration node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getId().accept(this);
    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getId().accept(this);
    }

    @Override
    public void visit(FunctionDeclaration node) throws ASTVisitorException {
        env.push(new LocalIndexPool());
        ASTUtils.setLocalIndexPool(node, env.element());
        if (node.getParams()!=null) for(ParameterDeclaration pd : node.getParams()) pd.accept(this);
        node.getStatement().accept(this);
        env.pop();
    }

    //Statements
    @Override
    public void visit(PrintStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression().accept(this);
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression().accept(this);
        node.getStatement().accept(this);
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression().accept(this);
        node.getStatement1().accept(this);
        node.getStatement2().accept(this);
    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression().accept(this);
        node.getStatement().accept(this);
    }

    @Override
    public void visit(DoWhileStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression().accept(this);
        node.getStatement().accept(this);
    }

    @Override
    public void visit(FunctionCallStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getFunct().accept(this);
    }

    @Override
    public void visit(ExitStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        if (node.getExpr() != null)
            node.getExpr().accept(this);
    }

    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        List<Statement> stmts = node.getStmtList();
        if(stmts != null) for (Statement s : stmts) {
            s.accept(this);
        }
    }

    //Expressions
    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        if(node.getIndexExpression()!= null) node.getIndexExpression().accept(this);
    }

    @Override
    public void visit(DoubleLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
    }

    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
    }

    @Override
    public void visit(CharacterLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
    }

    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
    }

    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression1().accept(this);
        node.getExpression2().accept(this);
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression().accept(this);
    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression().accept(this);
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        if(node.getExpressions()!=null) for (Expression e : node.getExpressions()) e.accept(this);
    }

    //Assignments
    @Override
    public void visit(BasicAssignment node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpr().accept(this);
    }

    @Override
    public void visit(ArrayInitAssignment node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        if(node.getSizeExpression() != null) node.getSizeExpression().accept(this);
    }

    @Override
    public void visit(ArrayIndexAssignment node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpr().accept(this);
        if(node.getIndexExpression() != null) node.getIndexExpression().accept(this);
        node.getId().accept(this);
    }

    @Override
    public void visit(CompoundAssign node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        if (node.getVardecl() != null) node.getVardecl().accept(this);
        node.getAssign().accept(this);

    }

}
