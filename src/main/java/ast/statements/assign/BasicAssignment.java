package ast.statements.assign;

import ast.ASTVisitor;
import ast.ASTVisitorException;
import ast.Assignment;
import ast.Expression;
import ast.exprs.IdentifierExpression;

public class BasicAssignment extends Assignment {

    private Expression expr;

    public BasicAssignment(IdentifierExpression id, Expression expr) {
        this.id = id;
        this.expr = expr;
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
