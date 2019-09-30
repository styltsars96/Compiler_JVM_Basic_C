/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.statements;

import ast.ASTVisitor;
import ast.ASTVisitorException;
import ast.Expression;
import ast.Statement;

/**
 *
 * @author Lazaros
 */
public class IfElseStatement extends Statement {

    private Expression expression;
    private Statement statement1;
    private Statement statement2;

    public IfElseStatement(Expression expression, Statement statement1, Statement statement2) {
        this.expression = expression;
        this.statement1 = statement1;
        this.statement2 = statement2;
    }

    public Expression getExpression() {
        return expression;
    }

    public Statement getStatement1() {
        return statement1;
    }

    public Statement getStatement2() {
        return statement2;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public void setStatement1(Statement statement1) {
        this.statement1 = statement1;
    }

    public void setStament2(Statement stament2) {
        this.statement2 = stament2;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
