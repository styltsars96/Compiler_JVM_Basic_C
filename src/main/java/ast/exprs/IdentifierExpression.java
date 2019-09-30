package ast.exprs;

import ast.ASTVisitor;
import ast.ASTVisitorException;
import ast.Expression;

public class IdentifierExpression extends Expression {
    //This is for any access of an idetifier.
    private String identifier;
    //For array index access with identifier.
    private Integer index;//When integer is given.
    private Expression indexExpression;//When an expression is given.

    public IdentifierExpression(String identifier) {
        this.identifier = identifier;
    }

    public IdentifierExpression(String identifier, Integer index) {
        this.identifier = identifier;
        this.index = index;
    }

    public IdentifierExpression(String identifier, Expression indexExpression) {
        this.identifier = identifier;
        this.indexExpression = indexExpression;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Expression getIndexExpression() {
        return this.indexExpression;
    }

    public void setIndexExpression(Expression indexExpression) {
        this.indexExpression = indexExpression;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
