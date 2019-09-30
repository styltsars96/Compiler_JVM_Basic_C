import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.objectweb.asm.Type;

import ast.*;
import ast.exprs.*;
import ast.statements.*;
import ast.statements.assign.*;
import symbol.*;
import types.TypeUtils;
/**
 * 1st Parse of AST
 * -Symbol Table Construction
 * -Assign nodes to their Symbol Tables
 * -Initiate standard library (print(String id))
 * -Parameters of function are treated as instantiated Variables,
 *	so parameters and local variables share same symbol table!
 */
public class SymTableBuilderASTVisitor implements ASTVisitor{

	private final Deque<SymTable<SymTableEntry>> env;
	private boolean mergeEnv;//default false, true if we shoud not push new env!

	public SymTableBuilderASTVisitor() {
		env = new ArrayDeque<SymTable<SymTableEntry>>();
		mergeEnv = false;
	}

	@Override
	public void visit(CompUnit node) throws ASTVisitorException {
		System.err.println();
		pushEnvironment();

		ASTUtils.setEnv(node, env.element());

		//Define print function. Gets one parameter. (Beginning of 1st Parse)
		ArrayList<ParameterDeclaration> params = new  ArrayList<ParameterDeclaration>();
		params.add(new ParameterDeclaration(false, new IdentifierExpression("id"), TypeUtils.STRING_TYPE ));
		//Function declaration for < print(String id) >.
		FunctionDeclaration printdecl = new FunctionDeclaration(Type.VOID_TYPE,new IdentifierExpression("print"),params,null);
		printdecl.accept(this);

		//Visit file's declarations.
		for (Declaration d : node.getDeclarations()) {
			d.accept(this);
		}
		popEnvironment();
	}

	//Declarations
	@Override
	public void visit(VariableDeclaration node) throws ASTVisitorException {
		ASTUtils.setEnv(node, env.element());
		node.getId().accept(this);
	}

	@Override
	public void visit(ParameterDeclaration node) throws ASTVisitorException {
		ASTUtils.setEnv(node, env.element());
		node.getId().accept(this);
	}

	@Override
	public void visit(FunctionDeclaration node) throws ASTVisitorException {
		ASTUtils.setEnv(node, env.element());
		pushEnvironment();//Function SymTable.
		mergeEnv = true;//CompStmt uses same SymTable!
		if (node.getParams() != null)
			for (Declaration d : node.getParams()) {
				d.accept(this);
		}
	//NOTE Compound Statement( { /*Statements*/  } )  SymTable is pushed when visited...
	//But if mergeEnv is true, it is not...
		if(node.getStatement()!=null)
				node.getStatement().accept(this);
		mergeEnv = false;//Symbol Table complete
		popEnvironment();
	}

	//Statements
	@Override
	public void visit(PrintStatement node) throws ASTVisitorException {
		ASTUtils.setEnv(node, env.element());
		node.getExpression().accept(this);
	}

	@Override
	public void visit(IfStatement node) throws ASTVisitorException {
		ASTUtils.setEnv(node, env.element());
		node.getExpression().accept(this);
		node.getStatement().accept(this);
	}

	@Override
	public void visit(IfElseStatement node) throws ASTVisitorException {
		ASTUtils.setEnv(node, env.element());
		node.getExpression().accept(this);
		node.getStatement1().accept(this);
		node.getStatement2().accept(this);
	}

	@Override
	public void visit(WhileStatement node) throws ASTVisitorException {
		ASTUtils.setEnv(node, env.element());
		node.getExpression().accept(this);
		node.getStatement().accept(this);
	}

	@Override
	public void visit(DoWhileStatement node) throws ASTVisitorException {
		ASTUtils.setEnv(node, env.element());
		node.getExpression().accept(this);
		node.getStatement().accept(this);
	}

	@Override
	public void visit(FunctionCallStatement node) throws ASTVisitorException {
		ASTUtils.setEnv(node, env.element());
		node.getFunct().accept(this);
	}

	@Override
	public void visit(ExitStatement node) throws ASTVisitorException {
		ASTUtils.setEnv(node, env.element());
		if (node.getExpr() != null)
			node.getExpr().accept(this);
	}

	@Override
	public void visit(CompoundStatement node) throws ASTVisitorException {
		//Each block ( {/*Statements*/} ) has its own SymTable, unless it belongs to a function
		if(!this.mergeEnv) pushEnvironment();
		ASTUtils.setEnv(node, env.element());
		List<Statement> stmts = node.getStmtList();
		if(stmts != null) for (Statement s : stmts) {
			s.accept(this);
		}
		if(!this.mergeEnv) popEnvironment();
	}

	//Expressions
	@Override
	public void visit(IdentifierExpression node) throws ASTVisitorException {
		ASTUtils.setEnv(node, env.element());
		if(node.getIndexExpression()!= null) node.getIndexExpression().accept(this);
	}

	@Override
	public void visit(DoubleLiteralExpression node) throws ASTVisitorException {
		ASTUtils.setEnv(node, env.element());
	}

	@Override
	public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
		ASTUtils.setEnv(node, env.element());
	}

	@Override
	public void visit(CharacterLiteralExpression node) throws ASTVisitorException {
		ASTUtils.setEnv(node, env.element());
	}

	@Override
	public void visit(StringLiteralExpression node) throws ASTVisitorException {
		ASTUtils.setEnv(node, env.element());
	}

	@Override
	public void visit(BinaryExpression node) throws ASTVisitorException {
		ASTUtils.setEnv(node, env.element());
		node.getExpression1().accept(this);
		node.getExpression2().accept(this);
	}

	@Override
	public void visit(UnaryExpression node) throws ASTVisitorException {
		ASTUtils.setEnv(node, env.element());
		node.getExpression().accept(this);
	}

	@Override
	public void visit(ParenthesisExpression node) throws ASTVisitorException {
		ASTUtils.setEnv(node, env.element());
		node.getExpression().accept(this);
	}

	@Override
	public void visit(FunctionCallExpression node) throws ASTVisitorException {
		ASTUtils.setEnv(node, env.element());
		if(node.getExpressions()!=null)
		for (Expression e : node.getExpressions()) {
			e.accept(this);
		}
	}

	//Assignments
	@Override
	public void visit(BasicAssignment node) throws ASTVisitorException {
		ASTUtils.setEnv(node, env.element());
		node.getExpr().accept(this);

	}

	@Override
	public void visit(ArrayInitAssignment node) throws ASTVisitorException {
		ASTUtils.setEnv(node, env.element());
		if(node.getSizeExpression() != null) node.getSizeExpression().accept(this);
	}

	@Override
	public void visit(ArrayIndexAssignment node) throws ASTVisitorException {
		ASTUtils.setEnv(node, env.element());
		node.getExpr().accept(this);
		if(node.getIndexExpression() != null) node.getIndexExpression().accept(this);
		node.getId().accept(this);
	}

	@Override
	public void visit(CompoundAssign node) throws ASTVisitorException {
		ASTUtils.setEnv(node, env.element());
		if (node.getVardecl() != null)
			node.getVardecl().accept(this);
		node.getAssign().accept(this);

	}

	//Symbol Table Stack Management
	private void pushEnvironment() {
		SymTable<SymTableEntry> oldSymTable = env.peek();
		SymTable<SymTableEntry> symTable = new HashSymTable<SymTableEntry>(oldSymTable);
		env.push(symTable);
	}

	private void popEnvironment() {
		env.pop();
	}
}
