
import org.objectweb.asm.Type;

import ast.*;
import ast.exprs.*;
import ast.statements.*;
import ast.statements.assign.*;
import symbol.*;
/**
 * 2nd Parse of AST.
 * -We have to collect symbol types,
 *  so we focus on variable, parameter and function declaration.
 * -Check for duplicates in the same scope
 * -Check that there exists only one main()
 *
 */
public class CollectSymbolsASTVisitor implements ASTVisitor {

	private boolean isGlobal;//Variable is in global scope.

	public CollectSymbolsASTVisitor() {
		//Begin at global scope, variables are static fields of class.
		this.isGlobal = true;
	}

	@Override
	public void visit(CompUnit node) throws ASTVisitorException {
		System.out.println("Collect Symbols phase");
		SymTable<SymTableEntry> table = ASTUtils.getEnv(node);

		//Declarations in file
		for (Declaration d : node.getDeclarations()) {
			d.accept(this);
		}

		//Check if there exists a 'void main(){\*Statements*\}' (End of 2nd Pass)
		SymTableEntry main = table.lookup("main",true);
		if(main != null){
//				System.out.println(main.getType().toString());//DEBUG
//				System.out.println("( occurs "+ main.getType().toString().indexOf('('));//DEBUG TEST
				//Check if main returns void and takes no params.
				if(main.getType().equals(Type.getMethodType(Type.VOID_TYPE)) || main.getType().equals(Type.getMethodType(Type.INT_TYPE))){
						System.out.println("Found main()");//DEBUG
				}else{
						System.out.println("Main should be declared like 'void main(){}'");
						ASTUtils.error(node, "main() function not properly declared!");
				}
		}else{
				ASTUtils.error(node, "No main() function declared!");
		}
	}

	// Declarations
	@Override
	public void visit(VariableDeclaration node) throws ASTVisitorException {
		SymTable<SymTableEntry> table = ASTUtils.getEnv(node);
		if (table.lookupOnlyInTop(node.getId().getIdentifier(), false) == null) {
			//  acquire active LocalIndexPool new
			LocalIndexPool pool = ASTUtils.getSafeLocalIndexPool(node);
			//  get free local index depending on variable type
			int poolIndex;
			if(this.isGlobal){
					poolIndex = -1;//Needed for later.
			}else poolIndex = pool.getLocalIndex(node.getType());

			table.put(node.getId().getIdentifier(), new SymTableEntry(node.getId().getIdentifier(), node.getType(),false,poolIndex));
			//System.err.println("Var Declaration: "+node.getType()+" "+node.getId().getIdentifier());
		} else {
			ASTUtils.error(node, "Duplicate local variable "+node.getId().getIdentifier());
		}
	}

	@Override
	public void visit(ParameterDeclaration node) throws ASTVisitorException {
		SymTable<SymTableEntry> table = ASTUtils.getEnv(node);
		if (table.lookupOnlyInTop(node.getId().getIdentifier(), false) == null) {
			//NOTE Parameters are treated as variables!
			//  acquire active LocalIndexPool new
			LocalIndexPool pool = ASTUtils.getSafeLocalIndexPool(node);//new
			//  get free local index depending on variable type
			int poolIndex = pool.getLocalIndex(node.getType());
			if(node.getIsArray()){
				table.put(node.getId().getIdentifier(), new SymTableEntry(node.getId().getIdentifier(), node.getType(), -1, poolIndex));
			}else	table.put(node.getId().getIdentifier(), new SymTableEntry(node.getId().getIdentifier(), node.getType(), poolIndex));
			//System.err.println("Param Declaration: "+node.getType()+" "+node.getId().getIdentifier()+"is Array: "+node.getIsArray());
		} else
			ASTUtils.error(node, "Duplicate function parameter "+node.getId().getIdentifier());

	}

	@Override
	public void visit(FunctionDeclaration node) throws ASTVisitorException {
		this.isGlobal = false;
		SymTable<SymTableEntry> table = ASTUtils.getEnv(node);
		//System.err.println("Funct Declaration: "+node.getType()+" "+node.getId().getIdentifier());
		if (table.lookupOnlyInTop(node.getId().getIdentifier(), true) == null) {
			if(node.getParams()!=null) {
				int length = node.getParams().size();
				Type[] paramsType = new Type[length];
				int i=0;
				while( i<length) {
					ParameterDeclaration p = node.getParams().get(i);
					p.accept(this);
					paramsType[i] = p.getType();
					i++;
				}
				node.setType(Type.getMethodType(node.getType(),paramsType));
			}else{
				node.setType(Type.getMethodType(node.getType()));
			}
			//Is a function and takes a composite type.
			table.put(node.getId().getIdentifier(), new SymTableEntry(node.getId().getIdentifier(),  node.getType(), true,null), true);
			//System.err.println(node.getType());//DEBUG ...
			node.getStatement().accept(this);
		} else ASTUtils.error(node, "Duplicate function declaration "+node.getId().getIdentifier());

		this.isGlobal = true;
	}

	// Statements
	@Override
	public void visit(PrintStatement node) throws ASTVisitorException {
		node.getExpression().accept(this);
	}

	@Override
	public void visit(IfStatement node) throws ASTVisitorException {
		node.getExpression().accept(this);
		node.getStatement().accept(this);
	}

	@Override
	public void visit(IfElseStatement node) throws ASTVisitorException {
		node.getExpression().accept(this);
		node.getStatement1().accept(this);
		node.getStatement2().accept(this);
	}

	@Override
	public void visit(WhileStatement node) throws ASTVisitorException {
		node.getExpression().accept(this);
		node.getStatement().accept(this);
	}

	@Override
	public void visit(DoWhileStatement node) throws ASTVisitorException {
		node.getExpression().accept(this);
		node.getStatement().accept(this);
	}

	@Override
	public void visit(FunctionCallStatement node) throws ASTVisitorException {
		node.getFunct().accept(this);
	}

	@Override
	public void visit(ExitStatement node) throws ASTVisitorException {

	}

	@Override
	public void visit(CompoundStatement node) throws ASTVisitorException {
		if(node.getStmtList()!=null)
		for (Statement s : node.getStmtList()) {
			s.accept(this);
		}
	}

	// Expressions
	@Override
	public void visit(IdentifierExpression node) throws ASTVisitorException {
		if(node.getIndexExpression()!= null) node.getIndexExpression().accept(this);
	}

	@Override
	public void visit(DoubleLiteralExpression node) throws ASTVisitorException {
		// nothing
	}

	@Override
	public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
		// nothing

	}

	@Override
	public void visit(CharacterLiteralExpression node) throws ASTVisitorException {
		// nothing

	}

	@Override
	public void visit(StringLiteralExpression node) throws ASTVisitorException {
		// nothing
	}

	@Override
	public void visit(BinaryExpression node) throws ASTVisitorException {
		node.getExpression1().accept(this);
		node.getExpression2().accept(this);
	}

	@Override
	public void visit(UnaryExpression node) throws ASTVisitorException {
		node.getExpression().accept(this);
	}

	@Override
	public void visit(ParenthesisExpression node) throws ASTVisitorException {
		node.getExpression().accept(this);
	}

	@Override
	public void visit(FunctionCallExpression node) throws ASTVisitorException {
		if(node.getExpressions()!=null  )
		for (Expression e : node.getExpressions()) {
			e.accept(this);
		}
	}

	// Assignments
	@Override
	public void visit(BasicAssignment node) throws ASTVisitorException {
		node.getId().accept(this);
		node.getExpr().accept(this);
	}

	@Override
	public void visit(ArrayInitAssignment node) throws ASTVisitorException {
		if(node.getSizeExpression() != null) node.getSizeExpression().accept(this);

		SymTable<SymTableEntry> table = ASTUtils.getEnv(node);
		SymTableEntry e = table.lookup(node.getId().getIdentifier());

		if(e==null) ASTUtils.error(node, node.getId().getIdentifier()+" has not been declared.");
		if(!node.getType().equals(e.getType())) ASTUtils.error(node, node.getId().getIdentifier()+" : Array type mismatch.");
		//System.out.println("THE TYPE OF ARRAY IS: "+node.getType());
		if(e.getArraySize() != 0) ASTUtils.error(node, "This Array has been initialized.");
		Integer poolIndex = null;
		if(e.getIndex() != null){//If index is initialized...
				poolIndex = e.getIndex();//keep it.
		}else{//Otherwise, add new index
			LocalIndexPool pool =  ASTUtils.getSafeLocalIndexPool(node);// acquire active LocalIndexPool
			poolIndex = pool.getLocalIndex(node.getType());//  get free local index depending on variable type
		}
		if(node.getArraySize() != null){//Static Size
			//table.put(node.getId().getIdentifier(), new SymTableEntry(node.getId().getIdentifier(), node.getType(), node.getArraySize(), poolIndex));
			e.setArraySize(node.getArraySize());
			e.setIndex(poolIndex);
		}else if(node.getSizeExpression() != null){//Dynamic Size (symtable gets -1 as size!)
			//table.put(node.getId().getIdentifier(), new SymTableEntry(node.getId().getIdentifier(), node.getType(), -1 , poolIndex));
			e.setArraySize(-1);
			e.setIndex(poolIndex);
		}else ASTUtils.error(node, "Internal error: No size available for array.");
	}

	@Override
	public void visit(ArrayIndexAssignment node) throws ASTVisitorException {
		node.getExpr().accept(this);
		if(node.getIndexExpression() != null) node.getIndexExpression().accept(this);
		node.getId().accept(this);
		SymTable<SymTableEntry> table = ASTUtils.getEnv(node);
		SymTableEntry e = table.lookup(node.getId().getIdentifier());
		if(e==null) ASTUtils.error(node, node.getId().getIdentifier()+" has not been declared.");
		if(e.getArraySize()==0) ASTUtils.error(node, node.getId().getIdentifier()+" has not been initialized.");
		if(node.getIndex()!= null) //Check array bounds, if static index is given.
			if(e.getArraySize()>0){//If array size IS statically declared.
				if(e.getArraySize()<=node.getIndex() || node.getIndex()<0) ASTUtils.error(node, "Array index out of bounds.");
			}//Otherwise this has to be checked later.
	}

	@Override
	public void visit(CompoundAssign node) throws ASTVisitorException {
		if(node.getVardecl()!=null)
			node.getVardecl().accept(this);
		node.getAssign().accept(this);
	}
}
