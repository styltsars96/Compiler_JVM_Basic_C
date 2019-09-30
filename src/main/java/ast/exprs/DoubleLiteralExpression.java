/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package ast.exprs;

import ast.ASTVisitor;
import ast.ASTVisitorException;
import ast.Expression;

public class DoubleLiteralExpression extends Expression {

    private Double literal;

    public DoubleLiteralExpression(Double literal) {
        this.literal = literal;
    }

    public Double getLiteral() {
        return literal;
    }

    public void setLiteral(Double literal) {
        this.literal = literal;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
