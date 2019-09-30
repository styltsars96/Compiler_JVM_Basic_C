package ast.statements;

import ast.ASTVisitor;
import ast.ASTVisitorException;
import ast.Expression;
import ast.Statement;

public class DoWhileStatement extends RepetiousStatement {
	public DoWhileStatement(Statement statement, Expression expression) {
		super.expression = expression;
		super.statement = statement;
	}

	@Override
	public void accept(ASTVisitor visitor) throws ASTVisitorException {
		visitor.visit(this);
	}

}
