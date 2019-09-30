/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package ast.exprs;

import ast.ASTVisitor;
import ast.ASTVisitorException;
import ast.Expression;

public class IntegerLiteralExpression extends Expression {

    private Integer literal;

    public IntegerLiteralExpression(Integer literal) {
        this.literal = literal;
    }

    public Integer getLiteral() {
        return literal;
    }

    public void setLiteral(Integer literal) {
        this.literal = literal;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
