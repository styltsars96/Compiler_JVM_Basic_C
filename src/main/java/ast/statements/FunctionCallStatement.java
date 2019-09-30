/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.statements;

import ast.ASTVisitor;
import ast.ASTVisitorException;
import ast.Statement;
import ast.exprs.FunctionCallExpression;


public class FunctionCallStatement extends Statement{
    private FunctionCallExpression funct;

    public FunctionCallStatement(FunctionCallExpression funct) {
        this.funct = funct;
    }

    public FunctionCallExpression getFunct() {
        return funct;
    }

    public void setFunct(FunctionCallExpression funct) {
        this.funct = funct;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
