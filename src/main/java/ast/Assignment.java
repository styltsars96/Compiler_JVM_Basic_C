package ast;

import ast.exprs.IdentifierExpression;

public abstract class Assignment extends Statement {

    protected IdentifierExpression id;
    protected boolean isArray;

    public IdentifierExpression getId() {
        return id;
    }

    public void setId(IdentifierExpression id) {
        this.id = id;
    }
/*  //Useless
    public void setIsArray(boolean isArray) {
        this.isArray = isArray;
    }

    public boolean getIsArray() {
        return isArray;
    }
*/
}
