/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.exprs;

import ast.ASTVisitor;
import ast.ASTVisitorException;
import ast.Expression;

/**
 *
 * @author Lazaros
 */
public class ParenthesisExpression extends Expression{

    private Expression expression;

    public ParenthesisExpression(Expression expression) {
        this.expression = expression;
}

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
