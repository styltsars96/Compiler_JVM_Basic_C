package ast;

import ast.exprs.IdentifierExpression;

public abstract class Declaration extends Statement {
	public abstract IdentifierExpression getId();
}
