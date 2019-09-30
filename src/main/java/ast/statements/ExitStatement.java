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
public class ExitStatement extends Statement{

    private ExitType type;
    private Expression expr;

    public ExitStatement(ExitType type) {
        this.type = type;   
    }
    
    public ExitStatement(ExitType type, Expression expr) {
        if(type.equals(ExitType.RETURN)){
            this.expr = expr;
        }
        this.type = type;   
    }

    public ExitType getType() {
        return type;
    }

    public void setType(ExitType type) {
        this.type = type;
    }

    public Expression getExpr() {
        return expr;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
    }
    
    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
