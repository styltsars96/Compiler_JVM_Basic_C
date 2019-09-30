/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.exprs;

import ast.ASTVisitor;
import ast.ASTVisitorException;
import ast.Expression;


public class CharacterLiteralExpression extends Expression {

    private Character literal;

    public CharacterLiteralExpression(Character literal) {

        this.literal =literal;
    }

    public Character getLiteral() {
        return literal;
    }

    public void setLiteral(Character literal) {
        this.literal = literal;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
