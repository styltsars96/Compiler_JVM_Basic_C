package ast.statements;

import ast.Expression;
import ast.Statement;

public abstract class RepetiousStatement extends Statement{

    protected Expression expression;
    protected Statement statement;



    public Expression getExpression() {
        return expression;
    }

    public Statement getStatement() {
        return statement;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }
}
