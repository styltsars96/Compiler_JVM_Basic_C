import java.util.List;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import org.objectweb.asm.Type;
import threeaddr.*;
import ast.*;
import ast.exprs.*;
import ast.statements.*;
import ast.statements.assign.*;

import org.apache.commons.lang3.StringEscapeUtils;


public class IntermediateCodeASTVisitor /*implements ASTVisitor*/ {
//
//  private final Program program;
//  private final Deque<String> stack;
//  private int temp;
//
//  public IntermediateCodeASTVisitor() {
//    program = new Program();
//    stack = new ArrayDeque<String>();
//    temp = 0;
//  }
//
//  private String createTemp() {
//    return "t" + Integer.toString(temp++);
//  }
//
//  public Program getProgram() {
//    return program;
//  }
//
//	@Override
//	public void visit(CompUnit node) throws ASTVisitorException {
//    //Only Declarations operate at top scope.
//		for(Declaration d : node.getDeclarations()) d.accept(this);
//	}
//
//  //Declarations
//	@Override
//	public void visit(VariableDeclaration node) throws ASTVisitorException {
//		//Doesn't change the program at this point.
//	}
//
//	@Override
//	public void visit(ParameterDeclaration node) throws ASTVisitorException {
//    //TODO probably need parameter list for correct passing?
//    //Doesn't change the program at this point.
//	}
//
//	@Override
//	public void visit(FunctionDeclaration node) throws ASTVisitorException {
//    FuncDeclInstr f = new FuncDeclInstr(node.getId().getIdentifier());
//    program.add(f);
//		node.getStatement().accept(this);
//		Statement s = node.getStatement();
//
//    //Check if stray break or continue appears.
//    if (!ASTUtils.getBreakList(s).isEmpty()) {
//      ASTUtils.error(s, "Break detected without a loop.");
//    }
//    if (!ASTUtils.getContinueList(s).isEmpty()) {
//      ASTUtils.error(s, "Continue detected without a loop.");
//    }
//	}
//
////Statements
//	@Override
//	public void visit(PrintStatement node) throws ASTVisitorException {
//    node.getExpression().accept(this);
//    //Get parameter and create instruction
//    String t1 = stack.pop();
//    ParamInstr param = new ParamInstr(t1);
//    //Create a function call for print.
//    program.add(param);
//    program.add(new FunctCallInstr(null,"print",1));
//	}
//
//	@Override
//	public void visit(IfStatement node) throws ASTVisitorException {
//		ASTUtils.setBooleanExpression(node.getExpression(), true);
//
//    node.getExpression().accept(this);
//    ASTUtils.getNextList(node).addAll(ASTUtils.getFalseList(node.getExpression()));
//
//    LabelInstr beginStmtLabel = program.addNewLabel();
//    Program.backpatch(ASTUtils.getTrueList(node.getExpression()),beginStmtLabel);
//
//    node.getStatement().accept(this);
//
//    ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getStatement()));
//    ASTUtils.getBreakList(node).addAll(ASTUtils.getBreakList(node.getStatement()));
//    ASTUtils.getContinueList(node).addAll(ASTUtils.getContinueList(node.getStatement()));
//
//	}
//
//	@Override
//	public void visit(IfElseStatement node) throws ASTVisitorException {
//    ASTUtils.setBooleanExpression(node.getExpression(), true);
//
//    node.getExpression().accept(this);
//    LabelInstr Stmt1Label = program.addNewLabel();
//
//    node.getStatement1().accept(this);
//
//    LabelInstr Stmt2Label = program.addNewLabel();
//
//    node.getStatement2().accept(this);
//
//    Program.backpatch(ASTUtils.getTrueList(node.getExpression()),Stmt1Label);
//    Program.backpatch(ASTUtils.getFalseList(node.getExpression()),Stmt2Label);
//
//    ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getStatement1()));
//    ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getStatement2()));
//    ASTUtils.getBreakList(node).addAll(ASTUtils.getBreakList(node.getStatement1()));
//    ASTUtils.getBreakList(node).addAll(ASTUtils.getBreakList(node.getStatement2()));
//    ASTUtils.getContinueList(node).addAll(ASTUtils.getContinueList(node.getStatement1()));
//    ASTUtils.getContinueList(node).addAll(ASTUtils.getContinueList(node.getStatement2()));
//
//	}
//
//	@Override
//	public void visit(WhileStatement node) throws ASTVisitorException {
//	 ASTUtils.setBooleanExpression(node.getExpression(), true);
//
//  // create beginLabel
//	 LabelInstr beginlabel = program.addNewLabel();
//	 // see Program class for details
//   // produce code for expression
//   node.getExpression().accept(this);
//   // create beginStmtLabel
//	 LabelInstr beginstatement = program.addNewLabel();
//	 // backpatch truelist of expression with beginStmtLabel
//	 Program.backpatch(ASTUtils.getTrueList(node.getExpression()),beginstatement);
//	 // produce code for statement
//	 node.getStatement().accept(this);
//	 // backpatch nextlist of statement with beginLabel
//	 Program.backpatch(ASTUtils.getNextList(node.getStatement()),beginlabel);
//	 // backpatch continuelist of statement with beginLabel
//	 Program.backpatch(ASTUtils.getContinueList(node.getStatement()),beginlabel);
//	 // add GotoInstr to beginLabel
//	 program.add(new GotoInstr(beginlabel));
//	 // append falselist of expression into nextlist of node
//	 ASTUtils.getNextList(node).addAll(ASTUtils.getFalseList(node.getExpression()));
//	 // append breaklist of statement into nextlist of node
//	 ASTUtils.getNextList(node).addAll(ASTUtils.getBreakList(node.getStatement()));
//
//	}
//
//	@Override
//	public void visit(DoWhileStatement node) throws ASTVisitorException {
//    ASTUtils.setBooleanExpression(node.getExpression(), true);
//
//    LabelInstr beginLabel = program.addNewLabel();
//
//    node.getStatement().accept(this);
//    ASTUtils.getNextList(node).addAll(ASTUtils.getBreakList(node.getStatement()));
//
//    LabelInstr beginExprLabel = program.addNewLabel();
//    Program.backpatch(ASTUtils.getNextList(node.getStatement()), beginExprLabel);
//    Program.backpatch(ASTUtils.getContinueList(node.getStatement()), beginExprLabel);
//
//    node.getExpression().accept(this);
//    ASTUtils.getNextList(node).addAll(ASTUtils.getFalseList(node.getExpression()));
//    Program.backpatch(ASTUtils.getTrueList(node.getExpression()), beginLabel);
//
//	}
//
//	@Override
//	public void visit(FunctionCallStatement node) throws ASTVisitorException {
//		node.getFunct().accept(this);
//
//	}
//
//	@Override
//	public void visit(ExitStatement node) throws ASTVisitorException {
//
//		ExitType exit = node.getType();
//
//		switch(exit) {
//			case RETURN:
//        ReturnInstr ri;
//			  if(node.getExpr()!=null) {
//          node.getExpr().accept(this);
//          String temp = stack.pop();
//          ri= new ReturnInstr(temp);
//        }else
//          ri = new ReturnInstr();
//	        program.add(ri);
//
//	        GotoInstr i = new GotoInstr();
//	        // add instruction to returnlist of node
//	        ASTUtils.getNextList(node).add(i);
//				break;
//			case BREAK:
//		    GotoInstr bri= new GotoInstr();
//		    // add new GotoInstr to program
//		    program.add(bri);
//		    // add instruction to breaklist of node
//		    ASTUtils.getBreakList(node).add(bri);
//				break;
//			case CONTINUE:
//		    GotoInstr coni= new GotoInstr();
//		    // add new GotoInstr to program
//		    program.add(coni);
//		    // add instruction to continuelist of node
//		    ASTUtils.getContinueList(node).add(coni);
//				break;
//		}
//	}
//
//	@Override
//	public void visit(CompoundStatement node) throws ASTVisitorException {
//    Statement s = null, ps;
//    Iterator<Statement> it = node.getStmtList().iterator();
//    while (it.hasNext()) {
//      ps = s;
//      s = it.next();
//      if (ps != null && !ASTUtils.getNextList(ps).isEmpty()) {
//        Program.backpatch(ASTUtils.getNextList(ps), program.addNewLabel());
//      }
//      if (ps != null && !ASTUtils.getBreakList(ps).isEmpty()) {
//        ASTUtils.getBreakList(node).addAll(ASTUtils.getBreakList(ps));
//      }
//      if (ps != null && !ASTUtils.getContinueList(ps).isEmpty()) {
//        ASTUtils.getContinueList(node).addAll(ASTUtils.getContinueList(ps));
//      }
//        s.accept(this);
//        if(!ASTUtils.getBreakList(s).isEmpty())
//                ASTUtils.getBreakList(node).addAll(ASTUtils.getBreakList(s));
//        if(!ASTUtils.getContinueList(s).isEmpty())
//                ASTUtils.getContinueList(node).addAll(ASTUtils.getContinueList(s));
//    }
//    if (s != null && !ASTUtils.getNextList(s).isEmpty()) {
//      Program.backpatch(ASTUtils.getNextList(s), program.addNewLabel());
//    }
//	}
//
//	@Override
//	public void visit(IdentifierExpression node) throws ASTVisitorException {
//      if(node.getIndex()!=null) { //Static Array Index
//          String target = createTemp();
//          stack.push(target);
//          Type type = ASTUtils.getEnv(node).lookup(node.getIdentifier()).getType();//Type is passed for codegen usage.
//          program.add(new ArrIndAccessInstr(node.getIdentifier(), type, node.getIndex(), target));
//          //stack.push(node.getIdentifier()+"["+node.getIndex()+"]");
//      }else if(node.getIndexExpression() != null) { //Dynamic Array Index
//          node.getIndexExpression().accept(this);
//          String t = stack.pop();
//          String target = createTemp();
//          stack.push(target);
//          Type type = ASTUtils.getEnv(node).lookup(node.getIdentifier()).getType();//Type is passed for codegen usage.
//          program.add(new ArrIndAccessInstr(node.getIdentifier(), type, t, target));
//
//      }else //Simple Identifier
//		    stack.push(node.getIdentifier());
//	}
//
//	@Override
//	public void visit(DoubleLiteralExpression node) throws ASTVisitorException {
//    if (ASTUtils.isBooleanExpression(node)) {
//      if (node.getLiteral() != 0d) {
//        GotoInstr i = new GotoInstr();
//        program.add(i);
//        ASTUtils.getTrueList(node).add(i);
//      } else {
//        GotoInstr i = new GotoInstr();
//        program.add(i);
//        ASTUtils.getFalseList(node).add(i);
//      }
//    } else {
//      String t = createTemp();
//      stack.push(t);
//      program.add(new AssignInstr(node.getLiteral().toString(), t));
//    }
//
//	}
//
//	@Override
//	public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
//    if (ASTUtils.isBooleanExpression(node)) {
//      if (node.getLiteral() != 0) {
//        GotoInstr i = new GotoInstr();
//        program.add(i);
//        ASTUtils.getTrueList(node).add(i);
//      } else {
//        GotoInstr i = new GotoInstr();
//        program.add(i);
//        ASTUtils.getFalseList(node).add(i);
//      }
//    } else {
//      // create new temporary
//      String temp = createTemp();
//      // add AssignInstr to program
//      program.add(new AssignInstr(node.getLiteral().toString(),temp));
//      // add new temporary in stack
//      //stack.add(temp);
//      stack.push(temp);
//    }
//	}
//
//	@Override
//	public void visit(CharacterLiteralExpression node) throws ASTVisitorException {
//    // create new temporary
//    String temp = createTemp();
//    // add AssignInstr to program
//    program.add(new AssignInstr(" \'" +StringEscapeUtils.escapeJava(node.getLiteral().toString())+"\' ",temp));
//    // add new temporary in stack
//    //stack.add(temp);
//    stack.push(temp);
//	}
//
//	@Override
//	public void visit(StringLiteralExpression node) throws ASTVisitorException {
//    // create new temporary
//    String temp = createTemp();
//    // add AssignInstr to program
//    program.add(new AssignInstr(" \""+StringEscapeUtils.escapeJava(node.getLiteral())+"\" " ,temp));
//    // add new temporary in stack
//    //stack.add(temp);
//    stack.push(temp);
//	}
//
//	@Override
//	public void visit(BinaryExpression node) throws ASTVisitorException {
//    CondJumpInstr shortCircuit = null;//Jump instruction for short-circuit.
//    AssignInstr tempInstr = null;//In case of assignment short-circuit.
//
//    node.getExpression1().accept(this);
//    String t1 = stack.pop();
//
//    //For short-circuit code (AND)
//    if(node.getOperator().equals(Operator.AND)){
//      //if (t1 == fasle) skip the second expression, node is false
//      shortCircuit = new CondJumpInstr(Operator.IS_TRUE, t1);
//      program.add(shortCircuit);
//      ASTUtils.getFalseList(node).add(shortCircuit);//for later backpatch.
//      tempInstr = new AssignInstr("0");//for assignment.
//    }else
//    //For short-circuit code (OR)
//    if(node.getOperator().equals(Operator.OR)){
//      //if (t1 == true) skip the second expression, node is true
//      shortCircuit = new CondJumpInstr(Operator.IS_FALSE, t1);
//      program.add(shortCircuit);
//      ASTUtils.getTrueList(node).add(shortCircuit);//for later backpatch.
//      tempInstr = new AssignInstr("1");//for assignment.
//    }
//
//    //A little optimisation Notice for short-circuit
//    if((shortCircuit != null) &&
//    (!(node.getExpression1() instanceof IdentifierExpression || node.getExpression1() instanceof IntegerLiteralExpression)
//     && (node.getExpression2() instanceof IdentifierExpression || node.getExpression2() instanceof IntegerLiteralExpression))){
//      System.out.println("WARNING/NOTICE! Swap the operands of "+node.getOperator()+
//      " at "+ node.getLine()+":"+node.getColumn()+" for better performance");
//    }
//
//    node.getExpression2().accept(this);
//    String t2 = stack.pop();
//
//    if (ASTUtils.isBooleanExpression(node)) {//When inside while, if etc.
//      if (!node.getOperator().isRelational() && !node.getOperator().isBinaryBoolean()) {
//        ASTUtils.error(node, "A not boolean expression used as boolean.");
//      }
//
//      // create new CondJumpInstr with null target
//      CondJumpInstr i1 =new CondJumpInstr(node.getOperator(),t1,t2);
//      // create new GotoInstr with null target
//      GotoInstr i2= new GotoInstr(null);
//      // add both to program
//      program.add(i1); program.add(i2);
//      // add first instruction into truelist of node
//      ASTUtils.getTrueList(node).add(i1);
//      // add second instruction into falselist of node
//      ASTUtils.getFalseList(node).add(i2);
//      //short-circuit is backpatched later.
//
//    } else {//When not inside while, if etc.
//      // create new temporary
//      String temp = createTemp();
//      //add binary operation instruction
//      program.add(new BinaryOpInstr(node.getOperator(),t1,t2,temp));
//
//      if(shortCircuit!=null){//make short-circuit
//        GotoInstr jump = new GotoInstr();
//        program.add(jump); //Second operand ends here, jump the next assignment.
//
//        tempInstr.setResult(temp);
//        LabelInstr label = program.addNewLabel();
//        shortCircuit.setTarget(label);//shortCircuit leads here.
//        program.add(tempInstr);//Add short-circuit assignment.
//        LabelInstr label2 = program.addNewLabel();//Program continues....
//        jump.setTarget(label2);//Non s.s. continues here.
//      }
//
//      // add new temporary in stack
//      //stack.add(temp);
//      stack.push(temp);
//    }
//
//	}
//
//	@Override
//	public void visit(UnaryExpression node) throws ASTVisitorException {
//    node.getExpression().accept(this);
//    String t1 = stack.pop();
//    String t = createTemp();
//    stack.push(t);
//    program.add(new UnaryOpInstr(node.getOperator(), t1, t));
//
//	}
//
//	@Override
//	public void visit(ParenthesisExpression node) throws ASTVisitorException {
//    node.getExpression().accept(this);
//    String t1 = stack.pop();
//    String t = createTemp();
//    stack.push(t);
//    program.add(new AssignInstr(t1, t));
//
//	}
//
//	@Override
//	public void visit(FunctionCallExpression node) throws ASTVisitorException {
//    for(Expression e : node.getExpressions()) {
//      e.accept(this);
//      String t1 = stack.pop();
//      ParamInstr param = new ParamInstr(t1);
//      program.add(param);
//    }
//    String t = createTemp();
//    stack.push(t);
//    program.add(new FunctCallInstr(t,node.getFunctionName().getIdentifier(),node.getExpressions().size()));
//	}
//
//	@Override
//	public void visit(BasicAssignment node) throws ASTVisitorException {
//    node.getExpr().accept(this);
//    String t = stack.pop();
//    program.add(new AssignInstr(t, node.getId().getIdentifier()));
//	}
//
//	@Override
//	public void visit(ArrayInitAssignment node) throws ASTVisitorException {
//    node.getId().accept(this);
//    String t = stack.pop();
//    if(node.getSizeExpression() == null){//Static Size
//      program.add(new ArrayInitInstr(node.getArraySize(), node.getType(), t));
//    }else{//Dynamic Size
//      node.getSizeExpression().accept(this);
//      String s = stack.pop();
//      program.add(new ArrayInitInstr(s, node.getType(), t));
//    }
//
//	}
//
//	@Override
//	public void visit(ArrayIndexAssignment node) throws ASTVisitorException {
//    node.getExpr().accept(this);
//    String expr=stack.pop();
//    //Type is passed for codegen usage.
//    Type type = ASTUtils.getEnv(node).lookup(node.getId().getIdentifier()).getType();
//    if(node.getIndexExpression() == null){//Static index
//      program.add(new ArrIndAssignInstr(node.getId().getIdentifier(),type,node.getIndex(),expr));
//    }else{ //Dynamic Index
//      node.getIndexExpression().accept(this);
//      String iExpr=stack.pop();
//      program.add(new ArrIndAssignInstr(node.getId().getIdentifier(),type,iExpr,expr));
//    }
//	}
//
//	@Override
//	public void visit(CompoundAssign node) throws ASTVisitorException {
//		//if(node.getVardecl()!=null) node.getVardecl().accept(this);//nothing
//		node.getAssign().accept(this);
//	}
}
