import java.util.List;

import org.objectweb.asm.Type;

import ast.*;
import ast.exprs.*;
import ast.statements.*;
import ast.statements.assign.*;
import symbol.*;
import types.*;

/**
 * 3rd Parse of AST.
 * -We have to calculate types ,
 *  so we focus on statements.
 * -All nodes get an evaluated type.
 * -All types are checked.
 *
 */
public class CollectTypesASTVisitor implements ASTVisitor{
	int loops;//How many loops are currently open.

	Type functReturnType;//Is it inside a function?

	@Override
	public void visit(CompUnit node) throws ASTVisitorException {
		System.out.println("Collect Types phase");
		this.loops=0;

		//Declarations in file
		for (Declaration d : node.getDeclarations()) {
			d.accept(this);
		}

		ASTUtils.setType(node, Type.VOID_TYPE);//That just means it is visited.
	}

	//Declarations evaluate to void!
	@Override
	public void visit(VariableDeclaration node) throws ASTVisitorException {
		ASTUtils.setType(node, Type.VOID_TYPE);
		 //System.err.println("VariableDeclaration");
	}

	@Override
	public void visit(ParameterDeclaration node) throws ASTVisitorException {
		ASTUtils.setType(node, node.getType());
		 //System.err.println("ParameterDeclaration");
	}

	@Override
	public void visit(FunctionDeclaration node) throws ASTVisitorException {
		if(this.functReturnType != null) ASTUtils.error(node, "You cannot define a function inside another one.");
		this.functReturnType=node.getType().getReturnType();
		node.getStatement().accept(this);
		this.functReturnType=null;
		ASTUtils.setType(node, Type.VOID_TYPE);
	}

	//Statements
	@Override
	public void visit(PrintStatement node) throws ASTVisitorException {
		/*NOTE FOR LATER:
		 print expects only String! In order to avoid overloading
		 we cast/change everything to String in a special case.*/
		node.getExpression().accept(this);
		ASTUtils.setType(node, Type.VOID_TYPE);
	}

	@Override
	public void visit(IfStatement node) throws ASTVisitorException {
		node.getExpression().accept(this);
		if ((!ASTUtils.getSafeType(node.getExpression()).equals(Type.BOOLEAN_TYPE))
			&& (!ASTUtils.getSafeType(node.getExpression()).equals(Type.INT_TYPE))
		) {
      ASTUtils.error(node.getExpression(), "Invalid expression, should be boolean");
    }
    node.getStatement().accept(this);
    ASTUtils.setType(node, Type.VOID_TYPE);
	}

	@Override
	public void visit(IfElseStatement node) throws ASTVisitorException {
    node.getExpression().accept(this);
    if ((!ASTUtils.getSafeType(node.getExpression()).equals(Type.BOOLEAN_TYPE))
			&& (!ASTUtils.getSafeType(node.getExpression()).equals(Type.INT_TYPE))
		) {
      ASTUtils.error(node.getExpression(), "Invalid expression, should be boolean");
    }
    node.getStatement1().accept(this);
    node.getStatement2().accept(this);
    ASTUtils.setType(node, Type.VOID_TYPE);
	}

	@Override
	public void visit(WhileStatement node) throws ASTVisitorException {
		node.getExpression().accept(this);
		if ((!ASTUtils.getSafeType(node.getExpression()).equals(Type.BOOLEAN_TYPE))
			&& (!ASTUtils.getSafeType(node.getExpression()).equals(Type.INT_TYPE))
		) {
    	ASTUtils.error(node.getExpression(), "Invalid expression, should be boolean");
    }
    loops++;
    node.getStatement().accept(this);
		loops--;
    ASTUtils.setType(node, Type.VOID_TYPE);

	}

	@Override
	public void visit(DoWhileStatement node) throws ASTVisitorException {
		loops++;
		node.getStatement().accept(this);
		loops--;
		node.getExpression().accept(this);
		if ((!ASTUtils.getSafeType(node.getExpression()).equals(Type.BOOLEAN_TYPE))
		 && (!ASTUtils.getSafeType(node.getExpression()).equals(Type.INT_TYPE))
	 ) {
	            ASTUtils.error(node.getExpression(), "Invalid expression, should be boolean");
	        }
		 ASTUtils.setType(node, Type.VOID_TYPE);
	}

	@Override
	public void visit(FunctionCallStatement node) throws ASTVisitorException {
		node.getFunct().accept(this);
		ASTUtils.setType(node, Type.VOID_TYPE);//It returns nothing, no matter what.
	}

	@Override
	public void visit(ExitStatement node) throws ASTVisitorException {
		ASTUtils.setType(node, Type.VOID_TYPE);

		//Check for illegal break, continue, return.
		ExitType exit = node.getType();
//		System.out.println("Exit type is: "+ exit);//DEBUG
		switch ( exit ) {
		case RETURN :
			if(this.functReturnType == null) {
				ASTUtils.error(node, "This return is not inside a function.");
			}
			//Check if RVT does not evaluate to the function's RVT
			Expression retEx = node.getExpr();
			if(retEx==null){
				if(this.functReturnType != Type.VOID_TYPE) {
					ASTUtils.error(node, "Type mismatch: cannot convert from void to "+this.functReturnType);
				}
			}else{
				retEx.accept(this);
				if(this.functReturnType != ASTUtils.getType(node.getExpr())) {//Function RVT has to be same as expression evaluated type.
					//In the case of booleans, If function RVT is Integer, boolean can be returned.
					if(!(this.functReturnType == Type.INT_TYPE ? TypeUtils.isAssignable(this.functReturnType, ASTUtils.getType(node.getExpr())) : false)){
						ASTUtils.error(node, "Type mismatch: cannot convert from "+ASTUtils.getType(node.getExpr())+" to "+this.functReturnType);
					}
				}
			}
			break;
		case BREAK:
			if(loops<1) {
				ASTUtils.error(node, "This is a random break.");
			}
			break;
		case CONTINUE:
			if(loops<1) {
				ASTUtils.error(node, "This is a random continue.");
			}
			break;
		}
	}

	@Override
	public void visit(CompoundStatement node) throws ASTVisitorException {
		for(Statement s : node.getStmtList()) {
			s.accept(this);
		}
		ASTUtils.setType(node,Type.VOID_TYPE);
	}

	@Override
	public void visit(IdentifierExpression node) throws ASTVisitorException {
				//Dynamic array index case, where the expression has to be evaluated.
				if(node.getIndexExpression()!=null) node.getIndexExpression().accept(this);

				// 1. find symbol table
        SymTable<SymTableEntry> table = ASTUtils.getEnv(node);
        // 2. lookup identifier in symbol table
        SymTableEntry entry = table.lookup(node.getIdentifier());//Variable
        if(entry!=null) {
        	Type temp = entry.getType();
					if(node.getIndexExpression()!=null) {//Array Access, dynamic index
        		temp = Type.getType(entry.getType().getDescriptor().replace("[", ""));
						Type t = ASTUtils.getType(node.getIndexExpression());
						if(!t.equals(Type.INT_TYPE)) ASTUtils.error(node, "Type mismatch: Index expression has to return integer, not " + t);
        		ASTUtils.setType(node, temp);
        		return;
        	}
        	if(node.getIndex()!=null && node.getIndex()>=0) {//Array Access, static index
						//If index is statically defined AND it is already defined, check for out of bounds.
        		if(entry.getArraySize() > 0)	if(node.getIndex() >= entry.getArraySize())
							ASTUtils.error(node,"Index out of bounds on "+node.getIdentifier()+" . Size is: "+ entry.getArraySize());
        		temp = Type.getType(entry.getType().getDescriptor().replace("[", ""));
        		ASTUtils.setType(node, temp);
        		return;
        	}
        }

				if(entry==null) {
						entry = table.lookup(node.getIdentifier() , true);//Find Function
				}
        if(entry==null){
            // 3. error if not found
             ASTUtils.error(node,"Unresolved Type: "+node.getIdentifier());
        }
        // 4. set type of expression from symbol table
        ASTUtils.setType(node, entry.getType());
	}

	@Override
	public void visit(DoubleLiteralExpression node) throws ASTVisitorException {
		ASTUtils.setType(node, Type.DOUBLE_TYPE);
	}

	@Override
	public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
		ASTUtils.setType(node, Type.INT_TYPE);
	}

	@Override
	public void visit(CharacterLiteralExpression node) throws ASTVisitorException {
		ASTUtils.setType(node, Type.CHAR_TYPE);
	}

	@Override
	public void visit(StringLiteralExpression node) throws ASTVisitorException {
		ASTUtils.setType(node, Type.getType(String.class));
	}

	@Override
	public void visit(BinaryExpression node) throws ASTVisitorException {
		node.getExpression1().accept(this);
        node.getExpression2().accept(this);
        // 1. find type of expression1
        Type type1 = ASTUtils.getType(node.getExpression1());
        // 2. find type of expression 2
        Type type2 = ASTUtils.getType(node.getExpression2());
//        System.out.println(type1+" "+type2+" "+node.getOperator());
        try {
        	// 3. Use TypeUtils.applyBinary to figure type of result

			Type result = TypeUtils.applyBinary(node.getOperator(), type1, type2);
			// 5. set type of result
			ASTUtils.setType(node, result);
			//System.err.println(node.getLine()+"  BinaryExpression"+result);
		} catch (TypeException e) {
			// 4. error if TypeException
			ASTUtils.error(node, "Operation not allowed. "+e.getMessage());
		}
	}

	@Override
	public void visit(UnaryExpression node) throws ASTVisitorException {
		node.getExpression().accept(this);
        try {
            ASTUtils.setType(node, TypeUtils.applyUnary(node.getOperator(), ASTUtils.getSafeType(node.getExpression())));
        } catch (TypeException e) {
            ASTUtils.error(node, e.getMessage());
        }
	}

	@Override
	public void visit(ParenthesisExpression node) throws ASTVisitorException {
		node.getExpression().accept(this);
        ASTUtils.setType(node, ASTUtils.getSafeType(node.getExpression()));
		 //System.err.println(node.getLine()+"  ParenthesisExpression ");
	}

	@Override
	public void visit(FunctionCallExpression node) throws ASTVisitorException {
		SymTable<SymTableEntry> env = ASTUtils.getSafeEnv(node);
		SymTableEntry defentry=env.lookup(node.getFunctionName().getIdentifier(), true);//look for function with given name
		//System.out.println("Function:"+ node.getFunctionName().getIdentifier()+ "found:"+ defentry );//DEBUG
		if (defentry == null ) ASTUtils.error(node, "Undefined Method with name: "+node.getFunctionName().getIdentifier());

		Type [] defparam=defentry.getType().getArgumentTypes();

		//if function has not params go direct to ASTUtils.setType().
		if (!(defparam.length==0 && node.getExpressions()==null)) {

			if(defparam.length!=node.getExpressions().size()) ASTUtils.error(node, "You have to pass correct number of parameters to the function.");

			if(node.getExpressions()!=null) {
				int i=0;
				List<Expression> exprs = node.getExpressions();
				while(i<exprs.size()){
					Expression e=exprs.get(i);
					e.accept(this);
					Type expressionType =(Type) e.getProperty(ASTUtils.TYPE_PROPERTY);
					System.err.println(defparam[i].getDescriptor()+" VS "+expressionType.getDescriptor());
					if(! expressionType.getDescriptor().equals(defparam[i].getDescriptor())) {
						//System.err.println("That's wrong");
						ASTUtils.error(node, "There is a problem with arguments on method: "+node.getFunctionName().getIdentifier());
					}
					i++;
				}
			}
		}
		ASTUtils.setType(node, defentry.getType().getReturnType());
	}

	@Override
	public void visit(BasicAssignment node) throws ASTVisitorException {
        // 1. find symbol table
        SymTable<SymTableEntry> table = ASTUtils.getEnv(node);
        // 2. lookup identifier in symbol table
        SymTableEntry e = table.lookup(node.getId().getIdentifier());
        // 3. error if not found
        if(e==null) ASTUtils.error(node, node.getId().getIdentifier()+" can not resolved to a variable.");
        // 4. get expression type
        node.getExpr().accept(this);
        Type type = ASTUtils.getType(node.getExpr());
        // 5. Error if types are not assignable
        //    Use TypeUtils class with helper functions
        if(!TypeUtils.isAssignable(e.getType(), type)) {
        	ASTUtils.error(node, "Type mismatch: Cannot convert from "+type+" to "+e.getType());
        }
        // 6. set type of statement to VOID_TYPE
		ASTUtils.setType(node, Type.VOID_TYPE);
	}

	@Override
	public void visit(ArrayInitAssignment node) throws ASTVisitorException {

		//Array Types for initialization are already checked in 2nd pass.
		SymTable<SymTableEntry> table = ASTUtils.getEnv(node);
		SymTableEntry e = table.lookup(node.getId().getIdentifier());//look for variable
		if(e==null) ASTUtils.error(node, node.getId().getIdentifier()+" can not resolved to a variable.");

		if(node.getSizeExpression() != null){//Check if size expression is integer.
				node.getSizeExpression().accept(this);//evaluate expression.
				Type t = ASTUtils.getType(node.getSizeExpression());
				if(!t.equals(Type.INT_TYPE)){
						ASTUtils.error(node, "Type mismatch: Size expression has to return integer, not " + t);
				}
		}

		ASTUtils.setType(node, Type.VOID_TYPE);
	}

	@Override
	public void visit(ArrayIndexAssignment node) throws ASTVisitorException {
		SymTable<SymTableEntry> table = ASTUtils.getEnv(node);
    // 2. lookup identifier in symbol table
    SymTableEntry e = table.lookup(node.getId().getIdentifier());//for variable
    // 3. error if not found
    if(e==null) ASTUtils.error(node, node.getId().getIdentifier()+" can not resolved to a variable.");

		//Accept expression(s)
		node.getExpr().accept(this);
		if (node.getIndexExpression() != null) node.getIndexExpression().accept(this);

		Type t1 = e.getType();//Identifier
		if(!t1.getDescriptor().contains("[")) {
			ASTUtils.error(node, "Type should be array!");
		}
		t1 = Type.getType(t1.getDescriptor().replace("[", ""));

		Type t2 = ASTUtils.getType(node.getExpr());//Expression to evaluate
		if(!TypeUtils.areComparable(t1, t2)) {
        	ASTUtils.error(node, "Type mismatch: Cannot convert from  "+t2+" to "+t1);
    }

		if (node.getIndexExpression() != null){
			Type t3 = ASTUtils.getType(node.getIndexExpression());
			if(!t3.equals(Type.INT_TYPE)) ASTUtils.error(node, "Type mismatch: Index expression has to return integer, not " + t3);
		}
		ASTUtils.setType(node, Type.VOID_TYPE);
	}

	@Override
	public void visit(CompoundAssign node) throws ASTVisitorException {
		if(node.getVardecl()!=null)
		node.getVardecl().accept(this);
		node.getAssign().accept(this);
		ASTUtils.setType(node, Type.VOID_TYPE);
	}
}
