package ast.statements.assign;

import ast.ASTVisitor;
import ast.ASTVisitorException;
import ast.Expression;
import ast.exprs.IdentifierExpression;


public class ArrayIndexAssignment extends BasicAssignment{
    //This is for assigning to a specific array index.
    private Integer index;//When an integer is given
    private Expression indexExpression;//When an expression is given

    public ArrayIndexAssignment(IdentifierExpression id,Integer index, Expression expr) {
        super(id, expr);
        this.index=index;
        this.indexExpression = null;
    }

    public ArrayIndexAssignment(IdentifierExpression id, Expression indexExpression, Expression expr) {
        super(id, expr);
        this.index=null;
        this.indexExpression = indexExpression;
    }

    public Expression getIndexExpression() {
        return this.indexExpression;
    }

    public void setIndex(Expression indexExpression) {
        this.indexExpression = indexExpression;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }
     @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
}
