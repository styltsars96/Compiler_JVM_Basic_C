/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.exprs;

import ast.ASTVisitor;
import ast.ASTVisitorException;
import ast.Expression;
import java.util.List;

/**
 *
 * @author Lazaros
 */
public class FunctionCallExpression extends Expression {

    private IdentifierExpression functionName; //String --> IdentifierExpression
    private List<Expression> expressions;

    public FunctionCallExpression(IdentifierExpression functionName, List<Expression> expressions) {
        this.functionName = functionName;
        this.expressions = expressions;
    }

    public IdentifierExpression getFunctionName() {
        return functionName;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public void setFunctionName(IdentifierExpression functionName) {
        this.functionName = functionName;
    }

    public void setExpressions(List<Expression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
