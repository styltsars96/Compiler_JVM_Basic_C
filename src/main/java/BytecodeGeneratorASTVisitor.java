import ast.*;
import ast.exprs.*;
import ast.statements.*;
import ast.statements.assign.ArrayIndexAssignment;
import ast.statements.assign.ArrayInitAssignment;
import ast.statements.assign.BasicAssignment;
import ast.statements.assign.CompoundAssign;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import symbol.LocalIndexPool;
import symbol.SymTable;
import symbol.SymTableEntry;
import types.TypeUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BytecodeGeneratorASTVisitor implements ASTVisitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BytecodeGeneratorASTVisitor.class);

    private ClassNode cn;//The class we are building.
    private MethodNode mn;//Temporary holder of methodNode.
    private boolean isGlobal;//Variable is in global scope.
    private List<String> declarations;//Temporary declarations of Variables within function.

    public BytecodeGeneratorASTVisitor() {
        this.declarations = new ArrayList<String>();
        // create class
        cn = new ClassNode();
        cn.access = Opcodes.ACC_PUBLIC;
        cn.version = Opcodes.V1_5;
        cn.name = "Program";
        cn.sourceFile = "Program.in";
        cn.superName = "java/lang/Object";

        // create constructor
        mn = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V"));
        mn.instructions.add(new InsnNode(Opcodes.RETURN));
        mn.maxLocals = 1;
        mn.maxStack = 1;
        cn.methods.add(mn);

        //Begin at global scope, variables are static fields of class.
        this.isGlobal = true;
    }

    public ClassNode getClassNode() {
        return cn;
    }

    @Override
    public void visit(CompUnit node) throws ASTVisitorException {
        for(Declaration d : node.getDeclarations()){
            d.accept(this);
        }
    }

    @Override
    public void visit(VariableDeclaration node) throws ASTVisitorException {
        //Keep variable declaration in local scope temporarily.
        if(!this.isGlobal)this.declarations.add(node.getId().getIdentifier());
        if(this.isGlobal){
            /*NOTE: Would be useful if we had initialization on global (We don't allow it on Grammar).
            //Initial Values:
            Object init = null;
            if(node.getType().equals(Type.INT_TYPE)){

            }else if(node.getType().equals(Type.DOUBLE_TYPE)){

            }else if(node.getType().equals(Type.CHAR_TYPE)){

            }else if(node.getType().equals(TypeUtils.STRING_TYPE)){

            }
            */
            this.cn.fields.add(new FieldNode(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, node.getId().getIdentifier(), node.getType().getDescriptor(), null, null));
        }
    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        //Keep parameter declaration in local scope temporarily.
        if(!this.isGlobal)this.declarations.add(node.getId().getIdentifier());
    }

    @Override
    public void visit(FunctionDeclaration node) throws ASTVisitorException {
        this.isGlobal = false;//Not global
        mn = new MethodNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC , node.getId().getIdentifier(), node.getType().getDescriptor(), null, null);

        if(node.getParams() != null) for( ParameterDeclaration param : node.getParams()){
            param.accept(this);
        }
        node.getStatement().accept(this);
	    Statement s = node.getStatement();

        //Check if stray break or continue appears.
        if (!ASTUtils.getBreakList(s).isEmpty()) {
          ASTUtils.error(s, "Break detected without a loop.");
        }
        if (!ASTUtils.getContinueList(s).isEmpty()) {
          ASTUtils.error(s, "Continue detected without a loop.");
        }
        mn.instructions.add(new InsnNode(Opcodes.RETURN));//Just in case no return is found earlier in the execution.NOTE: it IS needed for void!
        mn.maxLocals = ASTUtils.getSafeLocalIndexPool(node).getMaxLocals() + 1;

        // IMPORTANT: this should be dynamically calculated
        // use COMPUTE_MAXS when computing the ClassWriter,
        // e.g. new ClassWriter(ClassWriter.COMPUTE_MAXS)
        mn.maxStack = 64;
        cn.methods.add(mn);
        this.isGlobal = true; //return to global scope
        this.declarations.clear();//empty temp declarations.
    }

    @Override
    public void visit(PrintStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);

        Type exprType = ASTUtils.getSafeType(node.getExpression());

        LocalIndexPool lip = ASTUtils.getSafeLocalIndexPool(node);
        int localIndex = lip.getLocalIndex(exprType);
        //Find if it is global variable or not.
        if(localIndex>=0){//Local variable
            mn.instructions.add(new VarInsnNode(exprType.getOpcode(Opcodes.ISTORE), localIndex));
            mn.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
            mn.instructions.add(new VarInsnNode(exprType.getOpcode(Opcodes.ILOAD), localIndex));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", Type.getMethodDescriptor(Type.VOID_TYPE, exprType)));
            lip.freeLocalIndex(localIndex, exprType);
        }else{//Global variable (check if it is truly identifier expression)
            if(!(node.getExpression() instanceof IdentifierExpression)) ASTUtils.error(node, "Global index for local variable!");
            IdentifierExpression id = (IdentifierExpression) node.getExpression();
            mn.instructions.add(new FieldInsnNode(Opcodes.PUTSTATIC, getLocalClassName(), id.getIdentifier(), exprType.getDescriptor()));
            mn.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
            mn.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, getLocalClassName(), id.getIdentifier(), exprType.getDescriptor()));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", Type.getMethodDescriptor(Type.VOID_TYPE, exprType)));
        }
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        node.getExpression().accept(this);
        ASTUtils.getNextList(node).addAll(ASTUtils.getFalseList(node.getExpression()));

        LabelNode labelNode = new LabelNode();
        mn.instructions.add(labelNode);
        backpatch(ASTUtils.getTrueList(node.getExpression()), labelNode);

        node.getStatement().accept(this);

        ASTUtils.getBreakList(node).addAll(ASTUtils.getBreakList(node.getStatement()));
        ASTUtils.getContinueList(node).addAll(ASTUtils.getContinueList(node.getStatement()));

        ASTUtils.getNextList(node).addAll(ASTUtils.getFalseList(node.getExpression()));
        ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getStatement()));

    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        node.getExpression().accept(this);

        LabelNode stmt1StartLabelNode = new LabelNode();
        mn.instructions.add(stmt1StartLabelNode);
        node.getStatement1().accept(this);

        JumpInsnNode skipGoto = new JumpInsnNode(Opcodes.GOTO, null);
        mn.instructions.add(skipGoto);

        LabelNode stmt2StartLabelNode = new LabelNode();
        mn.instructions.add(stmt2StartLabelNode);
        node.getStatement2().accept(this);

        backpatch(ASTUtils.getTrueList(node.getExpression()), stmt1StartLabelNode);
        backpatch(ASTUtils.getFalseList(node.getExpression()), stmt2StartLabelNode);

        ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getStatement1()));
        ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getStatement2()));
        ASTUtils.getNextList(node).add(skipGoto);

        ASTUtils.getBreakList(node).addAll(ASTUtils.getBreakList(node.getStatement1()));
        ASTUtils.getBreakList(node).addAll(ASTUtils.getBreakList(node.getStatement2()));

        ASTUtils.getContinueList(node).addAll(ASTUtils.getContinueList(node.getStatement1()));
        ASTUtils.getContinueList(node).addAll(ASTUtils.getContinueList(node.getStatement2()));
    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        LabelNode beginLabelNode = new LabelNode();
        mn.instructions.add(beginLabelNode);

        node.getExpression().accept(this);

        LabelNode trueLabelNode = new LabelNode();
        mn.instructions.add(trueLabelNode);
        backpatch(ASTUtils.getTrueList(node.getExpression()), trueLabelNode);

        node.getStatement().accept(this);

        backpatch(ASTUtils.getNextList(node.getStatement()), beginLabelNode);
        backpatch(ASTUtils.getContinueList(node.getStatement()), beginLabelNode);

        mn.instructions.add(new JumpInsnNode(Opcodes.GOTO, beginLabelNode));

        ASTUtils.getNextList(node).addAll(ASTUtils.getFalseList(node.getExpression()));
        ASTUtils.getNextList(node).addAll(ASTUtils.getBreakList(node.getStatement()));
    }

    @Override
    public void visit(DoWhileStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        LabelNode beginLabel = new LabelNode();
        mn.instructions.add(beginLabel);

        node.getStatement().accept(this);
        ASTUtils.getNextList(node).addAll(ASTUtils.getBreakList(node.getStatement()));

        LabelNode beginExprLabel = new LabelNode();
        mn.instructions.add(beginExprLabel);
        backpatch(ASTUtils.getNextList(node.getStatement()), beginExprLabel);
        backpatch(ASTUtils.getContinueList(node.getStatement()), beginExprLabel);

        node.getExpression().accept(this);
        ASTUtils.getNextList(node).addAll(ASTUtils.getFalseList(node.getExpression()));
        backpatch(ASTUtils.getTrueList(node.getExpression()), beginLabel);
    }

    @Override
    public void visit(FunctionCallStatement node) throws ASTVisitorException {
        node.getFunct().accept(this);
    }

    @Override
    public void visit(ExitStatement node) throws ASTVisitorException {
		switch(node.getType()) {
			case RETURN:
                Expression e = node.getExpr();
			    if(e!=null) {
                    e.accept(this);
                    mn.instructions.add(new InsnNode( ASTUtils.getSafeType(e).getOpcode(Opcodes.IRETURN)));
                }else mn.instructions.add(new InsnNode(Opcodes.RETURN));
				break;
			case BREAK:
                JumpInsnNode jmp = new JumpInsnNode(Opcodes.GOTO, null);
                mn.instructions.add(jmp);
                ASTUtils.getBreakList(node).add(jmp);
				break;
			case CONTINUE:
                JumpInsnNode contjmp = new JumpInsnNode(Opcodes.GOTO, null);
                mn.instructions.add(contjmp);
                ASTUtils.getContinueList(node).add(contjmp);
				break;
		}
    }

    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        Statement s = null, ps;
        Iterator<Statement> it = node.getStmtList().iterator();
        while (it.hasNext()) {
            ps = s;
            s = it.next();

            if (ps != null && !ASTUtils.getNextList(ps).isEmpty()) {
                LabelNode labelNode = new LabelNode();
                mn.instructions.add(labelNode);
                backpatch(ASTUtils.getNextList(ps), labelNode);
            }
            if (ps != null && !ASTUtils.getBreakList(ps).isEmpty()) {
                ASTUtils.getBreakList(node).addAll(ASTUtils.getBreakList(ps));
            }
            if (ps != null && !ASTUtils.getContinueList(ps).isEmpty()) {
                ASTUtils.getContinueList(node).addAll(ASTUtils.getContinueList(ps));
            }

            s.accept(this);

            if(!ASTUtils.getBreakList(s).isEmpty())
                ASTUtils.getBreakList(node).addAll(ASTUtils.getBreakList(s));
            if(!ASTUtils.getContinueList(s).isEmpty())
                ASTUtils.getContinueList(node).addAll(ASTUtils.getContinueList(s));
        }
        if (s != null && !ASTUtils.getNextList(s).isEmpty()) {
            LabelNode labelNode = new LabelNode();
            mn.instructions.add(labelNode);
            backpatch(ASTUtils.getNextList(s), labelNode);
        }

    }

    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        SymTable<SymTableEntry> symTable = ASTUtils.getSafeEnv(node);
        SymTableEntry id = null;
        //Find correct declaration.
        if(!this.isLocalDeclaration(node)){//If not local declaration.
            System.out.println("Lookup in Bottom for ID "+node.getIdentifier());//DEBUG
            id = symTable.lookupOnlyInBottom(node.getIdentifier());
            System.out.println("id "+id);//DEBUG
        }
        if(id == null){//If local declaration.
            id = symTable.lookup(node.getIdentifier());
        }
        //6 cases of access.
        if(id.getIndex()>=0){//FOR LOCAL variable
            if(node.getIndex()!=null) { //Static Array Index
                mn.instructions.add(new VarInsnNode(Opcodes.ALOAD,id.getIndex()));//Ref
                mn.instructions.add(new LdcInsnNode(node.getIndex()));
                mn.instructions.add(new InsnNode( ASTUtils.getSafeType(node).getOpcode(Opcodes.IALOAD)));//Access
            }else if(node.getIndexExpression() != null) { //Dynamic Array Index
                //Compute index.
                node.getIndexExpression().accept(this);
                Type exprType = ASTUtils.getSafeType(node.getIndexExpression());
                LocalIndexPool lip = ASTUtils.getSafeLocalIndexPool(node);
                int localIndex = lip.getLocalIndex(exprType);
                mn.instructions.add(new VarInsnNode(exprType.getOpcode(Opcodes.ISTORE), localIndex));
                //Array access
                mn.instructions.add(new VarInsnNode(Opcodes.ALOAD,id.getIndex()));//Ref
                mn.instructions.add(new VarInsnNode(exprType.getOpcode(Opcodes.ILOAD), localIndex));
                mn.instructions.add(new InsnNode( ASTUtils.getSafeType(node).getOpcode(Opcodes.IALOAD)));//Access
            }else{//Simple Identifier
                mn.instructions.add(new VarInsnNode( id.getType().getOpcode(Opcodes.ILOAD), id.getIndex()));//Access
            }

        }else{//FOR GLOBAL variable
            if(node.getIndex()!=null) { //Static Array Index
                mn.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, getLocalClassName(), id.getId(), id.getType().getDescriptor()));//Ref
                mn.instructions.add(new LdcInsnNode(node.getIndex()));
                mn.instructions.add(new InsnNode( ASTUtils.getSafeType(node).getOpcode(Opcodes.IALOAD)));//Access
            }else if(node.getIndexExpression() != null) { //Dynamic Array Index
                //Compute index.
                node.getIndexExpression().accept(this);
                Type exprType = ASTUtils.getSafeType(node.getIndexExpression());
                LocalIndexPool lip = ASTUtils.getSafeLocalIndexPool(node);
                int localIndex = lip.getLocalIndex(exprType);
                mn.instructions.add(new VarInsnNode(exprType.getOpcode(Opcodes.ISTORE), localIndex));
                mn.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, getLocalClassName(), id.getId(), id.getType().getDescriptor()));//Ref
                mn.instructions.add(new VarInsnNode(exprType.getOpcode(Opcodes.ILOAD), localIndex));
                mn.instructions.add(new InsnNode( ASTUtils.getSafeType(node).getOpcode(Opcodes.IALOAD)));//Access
            }else{//Simple Identifier
                mn.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, getLocalClassName(), id.getId(), id.getType().getDescriptor()));
            }
        }
    }

    @Override
    public void visit(DoubleLiteralExpression node) throws ASTVisitorException {
        if (ASTUtils.isBooleanExpression(node)) {
            JumpInsnNode i = new JumpInsnNode(Opcodes.GOTO, null);
            mn.instructions.add(i);
            if (node.getLiteral() != 0d) {
                ASTUtils.getTrueList(node).add(i);
            } else {
                ASTUtils.getFalseList(node).add(i);
            }
        } else {
            mn.instructions.add(new LdcInsnNode(node.getLiteral()));
        }
    }

    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        if (ASTUtils.isBooleanExpression(node)) {
            JumpInsnNode i = new JumpInsnNode(Opcodes.GOTO, null);
            mn.instructions.add(i);
            if (node.getLiteral() != 0) {
                ASTUtils.getTrueList(node).add(i);
            } else {
                ASTUtils.getFalseList(node).add(i);
            }
        } else {
            mn.instructions.add(new LdcInsnNode(node.getLiteral()));
        }
    }

    @Override
    public void visit(CharacterLiteralExpression node) throws ASTVisitorException {
        mn.instructions.add(new LdcInsnNode(node.getLiteral()));
    }

    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        mn.instructions.add(new LdcInsnNode(node.getLiteral()));
    }

    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        Type expr1Type = ASTUtils.getSafeType(node.getExpression1());
        Type expr2Type = ASTUtils.getSafeType(node.getExpression2());
        LocalIndexPool lip = ASTUtils.getSafeLocalIndexPool(node);

        //AND or OR only...
        if(node.getOperator().isBinaryBoolean()){
            if(!( (expr1Type.equals(Type.INT_TYPE) || expr1Type.equals(Type.BOOLEAN_TYPE)) &&
              (expr2Type.equals(Type.INT_TYPE) || expr2Type.equals(Type.BOOLEAN_TYPE)) ) ){
                ASTUtils.error(node, "Types are not boolean (integers).");
            }
            JumpInsnNode shortCircuit = null;//Jump instruction for short-circuit.
            InsnNode tempInstr = null;//In case of assignment short-circuit.

            node.getExpression1().accept(this);//Accept first expression.
            //Add the result of the expression in T1
            int localIndexT1 = lip.getLocalIndex(expr1Type);
            mn.instructions.add(new VarInsnNode(expr1Type.getOpcode(Opcodes.ISTORE), localIndexT1));

            //For short-circuit code (AND)
            if(node.getOperator().equals(Operator.AND)){
            //if (t1 == fasle) skip the second expression, node is false
                mn.instructions.add(new VarInsnNode(expr1Type.getOpcode(Opcodes.ILOAD), localIndexT1));//load expr1 result.
                mn.instructions.add(new InsnNode(Opcodes.ICONST_0));//load 0.
                shortCircuit = new JumpInsnNode(Opcodes.IF_ICMPEQ, null);//if !=0 is true...
                mn.instructions.add(shortCircuit);
                ASTUtils.getFalseList(node).add(shortCircuit);//for later backpatch.
                tempInstr = new InsnNode(Opcodes.ICONST_0);//for assignment. CHECK...
            }else
            //For short-circuit code (OR)
            if(node.getOperator().equals(Operator.OR)){
            //if (t1 == true) skip the second expression, node is true
                mn.instructions.add(new VarInsnNode(expr1Type.getOpcode(Opcodes.ILOAD), localIndexT1));//load expr1 result.
                mn.instructions.add(new InsnNode(Opcodes.ICONST_0));//load 0.
                shortCircuit = new JumpInsnNode(Opcodes.IF_ICMPNE, null);//if ==0 is false...
                mn.instructions.add(shortCircuit);
                ASTUtils.getTrueList(node).add(shortCircuit);//for later backpatch.
                tempInstr = new InsnNode(Opcodes.ICONST_1);//for assignment. CHECK...
            }

            node.getExpression2().accept(this);
            //OLD  String t2 = stack.pop();
            int localIndexT2 = lip.getLocalIndex(expr2Type);
            mn.instructions.add(new VarInsnNode(expr2Type.getOpcode(Opcodes.ISTORE), localIndexT2));//keep variable...

            //CHANGE EACH TO BE ONLY 0 or 1!
            LabelNode trueLabelNode1 = new LabelNode();
            mn.instructions.add(new VarInsnNode(expr1Type.getOpcode(Opcodes.ILOAD), localIndexT1));
            mn.instructions.add(new JumpInsnNode(Opcodes.IFNE,trueLabelNode1));//If 0 (is false)
            mn.instructions.add(new InsnNode(Opcodes.ICONST_0));//set 0
            LabelNode endLabelNode1 = new LabelNode();
            mn.instructions.add(new JumpInsnNode(Opcodes.GOTO, endLabelNode1));//end
            mn.instructions.add(trueLabelNode1);//If not 0 (is true)
            mn.instructions.add(new InsnNode(Opcodes.ICONST_1));//set 1
            mn.instructions.add(endLabelNode1);//end
            mn.instructions.add(new VarInsnNode(expr1Type.getOpcode(Opcodes.ISTORE), localIndexT1));//Set 0 or 1...
            LabelNode trueLabelNode2 = new LabelNode();
            mn.instructions.add(new VarInsnNode(expr2Type.getOpcode(Opcodes.ILOAD), localIndexT2));
            mn.instructions.add(new JumpInsnNode(Opcodes.IFNE,trueLabelNode2));//If 0 (is false)
            mn.instructions.add(new InsnNode(Opcodes.ICONST_0));//set 0
            LabelNode endLabelNode2 = new LabelNode();
            mn.instructions.add(new JumpInsnNode(Opcodes.GOTO, endLabelNode2));//end
            mn.instructions.add(trueLabelNode2);//If not 0 (is true)
            mn.instructions.add(new InsnNode(Opcodes.ICONST_1));//set 1
            mn.instructions.add(endLabelNode2);//end
            //mn.instructions.add(new VarInsnNode(expr2Type.getOpcode(Opcodes.ISTORE), localIndexT2));//Set 0 or 1... NO NEED

            mn.instructions.add(new VarInsnNode(expr1Type.getOpcode(Opcodes.ILOAD), localIndexT1));//load previous on stack.
            //both results ready...

            if (ASTUtils.isBooleanExpression(node)) {//When inside while, if etc.
          //OLD    CondJumpInstr i1 =new CondJumpInstr(node.getOperator(),t1,t2);
                // create the new corresponding Conditional Jump Instruction with null target.
                if(node.getOperator().equals(Operator.AND)){
                    mn.instructions.add(new InsnNode(Opcodes.IAND));//do AND
                }
                if(node.getOperator().equals(Operator.OR)){
                    mn.instructions.add(new InsnNode(Opcodes.IOR));//do OR
                }
                mn.instructions.add(new InsnNode(Opcodes.ICONST_1));//load 1 (true)
                JumpInsnNode i1 = new JumpInsnNode(Opcodes.IF_ICMPEQ, null);//check if result is true...
                mn.instructions.add(i1);
                // create new GotoInstr with null target
                JumpInsnNode i2 = new JumpInsnNode(Opcodes.GOTO,null);
                mn.instructions.add(i2);
          // OLD ^ add both to program ^: program.add(i1); program.add(i2);
                // add first instruction into truelist of node
                ASTUtils.getTrueList(node).add(i1);
                // add second instruction into falselist of node
                ASTUtils.getFalseList(node).add(i2);
                //short-circuit is backpatched later.

            } else {//When not inside while, if etc.
                //add binary operation instruction
                if(node.getOperator().equals(Operator.AND)){
                    mn.instructions.add(new InsnNode(Opcodes.IAND));//do AND
                }
                if(node.getOperator().equals(Operator.OR)){
                    mn.instructions.add(new InsnNode(Opcodes.IOR));//do OR
                }
                if(shortCircuit!=null){//make short-circuit
                    JumpInsnNode jump = new JumpInsnNode(Opcodes.GOTO, null);
                    mn.instructions.add(jump);//Second operand ends here, jump the next assignment.
                    LabelNode label = new LabelNode();
                    shortCircuit.label = label;//shortCircuit leads here.
                    mn.instructions.add(label);
                    mn.instructions.add(tempInstr);//Add short-circuit assignment.
                    LabelNode label2 = new LabelNode();//Program continues....
                    jump.label = label2;//Non s.s. continues here.
                    mn.instructions.add(label2);
                }else ASTUtils.error(node, "Short-Circuit Failed! AND or OR not used, and s.s. misfired.");
            }

            lip.freeLocalIndex(localIndexT1);
            lip.freeLocalIndex(localIndexT2);
            return;
        }

        //OPERATOR IS NOT BINARY BOOLEAN!!!!
        node.getExpression1().accept(this);
        node.getExpression2().accept(this);

        Type maxType = TypeUtils.maxType(expr1Type, expr2Type);

        //CASTS...
        // cast top of stack to max
        if (!maxType.equals(expr2Type)) {
            widen(maxType, expr2Type);
        }

        // cast second from top to max
        if (!maxType.equals(expr1Type)) {
            int localIndex = -1;
            if (expr2Type.equals(Type.DOUBLE_TYPE) || expr1Type.equals(Type.DOUBLE_TYPE)) {
                localIndex = lip.getLocalIndex(expr2Type);
                if(localIndex>=0){
                    mn.instructions.add(new VarInsnNode(expr2Type.getOpcode(Opcodes.ISTORE), localIndex));
                }//access global variable is same, no differentiation here...
            } else {
                mn.instructions.add(new InsnNode(Opcodes.SWAP));
            }
            widen(maxType, expr1Type);
            if (expr2Type.equals(Type.DOUBLE_TYPE) || expr1Type.equals(Type.DOUBLE_TYPE)) {
                if(localIndex>=0){
                    mn.instructions.add(new VarInsnNode(expr2Type.getOpcode(Opcodes.ILOAD), localIndex));
                    lip.freeLocalIndex(localIndex, expr2Type);
                }//access global variable is same, no differentiation here...
            } else {
                mn.instructions.add(new InsnNode(Opcodes.SWAP));
            }
        }

        //Handle Boolean operators.
        if (ASTUtils.isBooleanExpression(node)) {
            handleBooleanOperator(node, node.getOperator(), maxType);
        }/* else if (maxType.equals(TypeUtils.STRING_TYPE)) {
            mn.instructions.add(new InsnNode(Opcodes.SWAP));
            handleStringOperator(node, node.getOperator());
        }*/else {//Handle Number operators.
            handleNumberOperator(node, node.getOperator(), maxType);
        }
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);

        //Minus Operator
        if(node.getOperator().equals(Operator.MINUS)){
            //Negate number.
            mn.instructions.add(new InsnNode(ASTUtils.getSafeType(node.getExpression()).getOpcode(Opcodes.INEG)));
        }
        //NOT operator
        if(node.getOperator().equals(Operator.NOT)){
            LabelNode end = new LabelNode();
            LabelNode isTrue = new LabelNode();
            mn.instructions.add(new JumpInsnNode(ASTUtils.getSafeType(node.getExpression()).getOpcode(Opcodes.IFNE),isTrue));//If 0 (is false)
            mn.instructions.add(new InsnNode(Opcodes.ICONST_1));//set result to 1 (true)
            mn.instructions.add(new JumpInsnNode(Opcodes.GOTO,end));//end
            mn.instructions.add(isTrue);//mn.instructions.add(new LabelNode());//ELSE (is true)
            mn.instructions.add(new InsnNode(Opcodes.ICONST_0));//set result to 0 (false)
            mn.instructions.add(end);//end
        }

    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {
        if(node.getExpressions()!=null) for(Expression e :node.getExpressions())  e.accept(this);

        //set type
        SymTable<SymTableEntry> symTable = ASTUtils.getSafeEnv(node);
        SymTableEntry id = symTable.lookupOnlyInBottom(node.getFunctionName().getIdentifier(),true);//NOTE: all function declarations here are in bottom!
        Type t= id.getType();

        //call method, all methods are static so we use invokestatic for each new method.
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, cn.name, node.getFunctionName().getIdentifier(),t.getDescriptor() ));
    }

    @Override
    public void visit(BasicAssignment node) throws ASTVisitorException {
        node.getExpr().accept(this);
        Type exprType = ASTUtils.getSafeType(node.getExpr());
        SymTable<SymTableEntry> symTable = ASTUtils.getSafeEnv(node);
        SymTableEntry id = null;
        System.out.println(node.getId().getIdentifier() + " is local delcaration? : "+ this.isLocalDeclaration(node.getId().getIdentifier()));//DEBUG
        if(!this.isLocalDeclaration(node.getId().getIdentifier())){//If not local declaration.
            System.out.println("Lookup only in bottom for assignment for ID "+ node.getId().getIdentifier());//DEBUG
            id= symTable.lookupOnlyInBottom(node.getId().getIdentifier());
        }
        if(id == null){//If global declaration.
            id= symTable.lookup(node.getId().getIdentifier());
        }

        widen(id.getType(), exprType);
        if(id.getIndex()>=0){
            mn.instructions.add(new VarInsnNode(id.getType().getOpcode(Opcodes.ISTORE), id.getIndex()));
        }else{//Global variable.
            mn.instructions.add(new FieldInsnNode(Opcodes.PUTSTATIC, getLocalClassName(), id.getId(), id.getType().getDescriptor()));
        }
    }

    @Override
    public void visit(ArrayInitAssignment node) throws ASTVisitorException {
        SymTable<SymTableEntry> symTable = ASTUtils.getSafeEnv(node);
        SymTableEntry id = null;

        //Find correct declaration.
        if(!this.isLocalDeclaration(node.getId().getIdentifier())){//If not local declaration.
            //System.out.println("Lookup in Bottom for ID "+node.getIdentifier());//DEBUG
            id = symTable.lookupOnlyInBottom(node.getId().getIdentifier());
            //System.out.println("id "+id);//DEBUG
        }
        if(id == null){//If local declaration.
            id = symTable.lookup(node.getId().getIdentifier());
        }
        boolean isLocal = (id.getIndex()>=0);//FOR LOCAL OR GLOBAL variable

        if(node.getSizeExpression() == null){//Static size
            mn.instructions.add(new LdcInsnNode(node.getArraySize()));
        }else{//Dynamic size
            node.getSizeExpression().accept(this);
            //Type exprType = ASTUtils.getSafeType(node.getSizeExpression());
            //LocalIndexPool lip = ASTUtils.getSafeLocalIndexPool(node);
            //int localIndex = lip.getLocalIndex(exprType);
            //mn.instructions.add(new VarInsnNode(exprType.getOpcode(Opcodes.ISTORE), localIndex));
        }
        Type t = Type.getType(id.getType().getDescriptor().replace('[',' ').trim());
        //System.out.println(t.getDescriptor()+" "+t.getSort());//DEBUG

        //Type of array.
        int typeint=0;
        if(t.equals(Type.INT_TYPE)) typeint=Opcodes.T_INT;
        if(t.equals(Type.DOUBLE_TYPE)) typeint=Opcodes.T_DOUBLE;
        if(t.equals(Type.CHAR_TYPE)) typeint=Opcodes.T_CHAR;

        mn.instructions.add(new VarInsnNode(Opcodes.NEWARRAY ,typeint));//init.
        if(isLocal){
            mn.instructions.add(new VarInsnNode( Opcodes.ASTORE,id.getIndex()));//store ref. (local)
        }else{

            mn.instructions.add(new FieldInsnNode(Opcodes.PUTSTATIC, getLocalClassName(), id.getId(), id.getType().getDescriptor()));//store ref. (global)
        }

    }

    @Override
    public void visit(ArrayIndexAssignment node) throws ASTVisitorException {
        SymTable<SymTableEntry> symTable = ASTUtils.getSafeEnv(node);
        SymTableEntry id = null;

        //Find correct declaration.
        if(!this.isLocalDeclaration(node.getId().getIdentifier())){//If not local declaration.
            //System.out.println("Lookup in Bottom for ID "+node.getIdentifier());//DEBUG
            id = symTable.lookupOnlyInBottom(node.getId().getIdentifier());
            //System.out.println("id "+id);//DEBUG
        }
        if(id == null){//If local declaration.
            id = symTable.lookup(node.getId().getIdentifier());
        }

        boolean isLocal = (id.getIndex()>=0);//FOR LOCAL OR GLOBAL variable
        Type type = id.getType();

        if(node.getIndexExpression() == null){//Static index
            if(isLocal){
                mn.instructions.add(new VarInsnNode( Opcodes.ALOAD,id.getIndex()));//ref (local)
            }else{

              mn.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, getLocalClassName(), id.getId(), id.getType().getDescriptor()));//Ref (global)
            }
            mn.instructions.add(new LdcInsnNode(node.getIndex()));//index
            node.getExpr().accept(this);//value
            Type target=Type.getType(type.getDescriptor().replace('[',' ').trim());
            Type source =ASTUtils.getSafeType(node.getExpr());
            widen(target,source);
            mn.instructions.add(new InsnNode( target.getOpcode(Opcodes.IASTORE)));//access
        }else{ //Dynamic Index
            //Compute index.
            node.getIndexExpression().accept(this);
            Type exprType = ASTUtils.getSafeType(node.getIndexExpression());
            LocalIndexPool lip = ASTUtils.getSafeLocalIndexPool(node);
            int localIndex = lip.getLocalIndex(exprType);
            mn.instructions.add(new VarInsnNode(exprType.getOpcode(Opcodes.ISTORE), localIndex));
            if(isLocal){
                mn.instructions.add(new VarInsnNode( Opcodes.ALOAD,id.getIndex()));//ref (local)
            }else{

                mn.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, getLocalClassName(), id.getId(), id.getType().getDescriptor()));//Ref
            }
            mn.instructions.add(new VarInsnNode(exprType.getOpcode(Opcodes.ILOAD), localIndex));//index
            node.getExpr().accept(this);//value
            Type target=Type.getType(type.getDescriptor().replace('[',' ').trim());
            Type source =ASTUtils.getSafeType(node.getExpr());
            widen(target,source);
            mn.instructions.add(new InsnNode( target.getOpcode(Opcodes.IASTORE)));//access
        }
    }

    @Override
    public void visit(CompoundAssign node) throws ASTVisitorException {
        if(node.getVardecl()!=null) node.getVardecl().accept(this);
        node.getAssign().accept(this);

    }

    private void backpatch(List<JumpInsnNode> list, LabelNode labelNode) {
        if (list == null) {
            return;
        }
        for (JumpInsnNode instr : list) {
            instr.label = labelNode;
        }
    }

    /**
     * Cast the top of the stack to a particular type
     */
    private void widen(Type target, Type source) {
        if (source.equals(target)) {
            return;
        }

        if (source.equals(Type.BOOLEAN_TYPE)) {
            if (target.equals(Type.INT_TYPE)) {
                // nothing
            } else if (target.equals(Type.DOUBLE_TYPE)) {
                mn.instructions.add(new InsnNode(Opcodes.I2D));
            } else if (target.equals(TypeUtils.STRING_TYPE)) {
                mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Boolean", "toString", "(Z)Ljava/lang/String;"));
            }
        } else if (source.equals(Type.INT_TYPE)) {
            if (target.equals(Type.DOUBLE_TYPE)) {
                mn.instructions.add(new InsnNode(Opcodes.I2D));
            } else if (target.equals(TypeUtils.STRING_TYPE)) {
                mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;"));
            }
        } else if (source.equals(Type.DOUBLE_TYPE)) {
            if (target.equals(TypeUtils.STRING_TYPE)) {
                mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double", "toString", "(D)Ljava/lang/String;"));
            }
        }
    }

    private void handleBooleanOperator(Expression node, Operator op, Type type) throws ASTVisitorException {
        /*
        if(op.isBinaryBoolean()){//AND and OR...
            System.out.println("Binary Boolean in Boolean Expr...");
            List<JumpInsnNode> trueList = new ArrayList<JumpInsnNode>();
            if(!type.equals(Type.INT_TYPE)){
                ASTUtils.error(node, "Types are not boolean (integers).");
            }
            switch(op){//Operation.
                case OR:
                    mn.instructions.add(new InsnNode(Opcodes.IOR));
                      break;
                case AND:
                    mn.instructions.add(new InsnNode(Opcodes.IAND));
                      break;
                default:
                      break;
          }
          ASTUtils.setTrueList(node, trueList);
          List<JumpInsnNode> falseList = new ArrayList<JumpInsnNode>();
          JumpInsnNode jmp = new JumpInsnNode(Opcodes.GOTO, null);
          mn.instructions.add(jmp);
          falseList.add(jmp);
          ASTUtils.setFalseList(node, falseList);
        } else {//comparisons. */
            List<JumpInsnNode> trueList = new ArrayList<JumpInsnNode>();
            /*if (type.equals(TypeUtils.STRING_TYPE)) {
                mn.instructions.add(new InsnNode(Opcodes.SWAP));
                JumpInsnNode jmp = null;
                mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z"));
                switch (op) {
                    case EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFNE, null);
                        break;
                    case NOT_EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFEQ, null);
                        break;
                    default:
                        ASTUtils.error(node, "Operator not supported on strings");
                        break;
                }
                mn.instructions.add(jmp);
                trueList.add(jmp);
            } else */
            if (type.equals(Type.DOUBLE_TYPE)) {//compare doubles
                mn.instructions.add(new InsnNode(Opcodes.DCMPG));
                JumpInsnNode jmp = null;
                switch (op) {
                    case EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFEQ, null);
                        mn.instructions.add(jmp);
                        break;
                    case NOT_EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFNE, null);
                        mn.instructions.add(jmp);
                        break;
                    case GREATER:
                        jmp = new JumpInsnNode(Opcodes.IFGT, null);
                        mn.instructions.add(jmp);
                        break;
                    case GREATER_EQ:
                        jmp = new JumpInsnNode(Opcodes.IFGE, null);
                        mn.instructions.add(jmp);
                        break;
                    case LESS:
                        jmp = new JumpInsnNode(Opcodes.IFLT, null);
                        mn.instructions.add(jmp);
                        break;
                    case LESS_EQ:
                        jmp = new JumpInsnNode(Opcodes.IFLE, null);
                        mn.instructions.add(jmp);
                        break;
                    default:
                      ASTUtils.error(node, "Operator not supported");
                      break;
                }
                trueList.add(jmp);
            } else {
                JumpInsnNode jmp = null;
                switch (op) {
                    case EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IF_ICMPEQ, null);
                        mn.instructions.add(jmp);
                        break;
                    case NOT_EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IF_ICMPNE, null);
                        mn.instructions.add(jmp);
                        break;
                    case GREATER:
                        jmp = new JumpInsnNode(Opcodes.IF_ICMPGT, null);
                        mn.instructions.add(jmp);
                        break;
                    case GREATER_EQ:
                        jmp = new JumpInsnNode(Opcodes.IF_ICMPGE, null);
                        mn.instructions.add(jmp);
                        break;
                    case LESS:
                        jmp = new JumpInsnNode(Opcodes.IF_ICMPLT, null);
                        mn.instructions.add(jmp);
                        break;
                    case LESS_EQ:
                        jmp = new JumpInsnNode(Opcodes.IF_ICMPLE, null);
                        mn.instructions.add(jmp);
                        break;
                    default:
                        ASTUtils.error(node, "Operator not supported");
                        break;
                }
                trueList.add(jmp);
            }
            ASTUtils.setTrueList(node, trueList);
            List<JumpInsnNode> falseList = new ArrayList<JumpInsnNode>();
            JumpInsnNode jmp = new JumpInsnNode(Opcodes.GOTO, null);
            mn.instructions.add(jmp);
            falseList.add(jmp);
            ASTUtils.setFalseList(node, falseList);
        //}
    }
    /*
    private void handleStringOperator(ASTNode node, Operator op) throws ASTVisitorException {
        if (op.equals(Operator.PLUS)) {
            mn.instructions.add(new TypeInsnNode(Opcodes.NEW, "java/lang/StringBuilder"));
            mn.instructions.add(new InsnNode(Opcodes.DUP));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V"));
            mn.instructions.add(new InsnNode(Opcodes.SWAP));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
            mn.instructions.add(new InsnNode(Opcodes.SWAP));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;"));
        } else if (op.isRelational()) {
            LabelNode trueLabelNode = new LabelNode();
            switch (op) {
                case EQUAL:
                    mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z"));
                    mn.instructions.add(new JumpInsnNode(Opcodes.IFNE, trueLabelNode));
                    break;
                case NOT_EQUAL:
                    mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z"));
                    mn.instructions.add(new JumpInsnNode(Opcodes.IFEQ, trueLabelNode));
                    break;
                default:
                    ASTUtils.error(node, "Operator not supported on strings");
                    break;
            }
            mn.instructions.add(new InsnNode(Opcodes.ICONST_0));
            LabelNode endLabelNode = new LabelNode();
            mn.instructions.add(new JumpInsnNode(Opcodes.GOTO, endLabelNode));
            mn.instructions.add(trueLabelNode);
            mn.instructions.add(new InsnNode(Opcodes.ICONST_1));
            mn.instructions.add(endLabelNode);
        } else {
            ASTUtils.error(node, "Operator not recognized");
        }
    }
    */
    private void handleNumberOperator(ASTNode node, Operator op, Type type) throws ASTVisitorException {
        if (op.equals(Operator.PLUS)) {
            mn.instructions.add(new InsnNode(type.getOpcode(Opcodes.IADD)));
        } else if (op.equals(Operator.MINUS)) {
            mn.instructions.add(new InsnNode(type.getOpcode(Opcodes.ISUB)));
        } else if (op.equals(Operator.MULTIPLY)) {
            mn.instructions.add(new InsnNode(type.getOpcode(Opcodes.IMUL)));
        } else if (op.equals(Operator.DIVISION)) {
            mn.instructions.add(new InsnNode(type.getOpcode(Opcodes.IDIV)));
        } else if (op.equals(Operator.MOD)) {
            mn.instructions.add(new InsnNode(type.getOpcode(Opcodes.IREM)));
        } else if (op.isRelational()) {
            if (type.equals(Type.DOUBLE_TYPE)) {
                mn.instructions.add(new InsnNode(Opcodes.DCMPG));
                JumpInsnNode jmp = null;
                switch (op) {
                    case EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFEQ, null);
                        mn.instructions.add(jmp);
                        break;
                    case NOT_EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFNE, null);
                        mn.instructions.add(jmp);
                        break;
                    case GREATER:
                        jmp = new JumpInsnNode(Opcodes.IFGT, null);
                        mn.instructions.add(jmp);
                        break;
                    case GREATER_EQ:
                        jmp = new JumpInsnNode(Opcodes.IFGE, null);
                        mn.instructions.add(jmp);
                        break;
                    case LESS:
                        jmp = new JumpInsnNode(Opcodes.IFLT, null);
                        mn.instructions.add(jmp);
                        break;
                    case LESS_EQ:
                        jmp = new JumpInsnNode(Opcodes.IFLE, null);
                        mn.instructions.add(jmp);
                        break;
                    default:
                        ASTUtils.error(node, "Operator not supported");
                        break;
                }
                mn.instructions.add(new InsnNode(Opcodes.ICONST_0));
                LabelNode endLabelNode = new LabelNode();
                mn.instructions.add(new JumpInsnNode(Opcodes.GOTO, endLabelNode));
                LabelNode trueLabelNode = new LabelNode();
                jmp.label = trueLabelNode;
                mn.instructions.add(trueLabelNode);
                mn.instructions.add(new InsnNode(Opcodes.ICONST_1));
                mn.instructions.add(endLabelNode);
            } else if (type.equals(Type.INT_TYPE)) {
                LabelNode trueLabelNode = new LabelNode();
                switch (op) {
                    case EQUAL:
                        mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPEQ, trueLabelNode));
                        break;
                    case NOT_EQUAL:
                        mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPNE, trueLabelNode));
                        break;
                    case GREATER:
                        mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPGT, trueLabelNode));
                        break;
                    case GREATER_EQ:
                        mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPGE, trueLabelNode));
                        break;
                    case LESS:
                        mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPLT, trueLabelNode));
                        break;
                    case LESS_EQ:
                        mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPLE, trueLabelNode));
                        break;
                    default:
                        break;
                }
                mn.instructions.add(new InsnNode(Opcodes.ICONST_0));
                LabelNode endLabelNode = new LabelNode();
                mn.instructions.add(new JumpInsnNode(Opcodes.GOTO, endLabelNode));
                mn.instructions.add(trueLabelNode);
                mn.instructions.add(new InsnNode(Opcodes.ICONST_1));
                mn.instructions.add(endLabelNode);
            }else {
                ASTUtils.error(node, "Cannot compare such types.");
            }
        }else/* if(op.isBinaryBoolean()){
            System.out.println("Binary Boolean Number.");//DEBUG
            LabelNode trueLabelNode = new LabelNode();
            if(!type.equals(Type.INT_TYPE)){
                ASTUtils.error(node, "Types are not boolean (integers).");
            }

            switch(op){//Operation. NOTE JVM or and and are bitwize...
                case OR:
                  //mn.instructions.add(new InsnNode(Opcodes.IOR));
                  break;
                case AND:
                  //mn.instructions.add(new InsnNode(Opcodes.IAND));
                  break;
                default:
                  break;
            }
            // NOTE JVM BOOLEANS are recognised AS Z if they are 0 or 1.
            //->  Another change has to be made so that they become I again.../
            mn.instructions.add(new JumpInsnNode(Opcodes.IFNE,trueLabelNode));//If 0 (is false)
            mn.instructions.add(new InsnNode(Opcodes.ICONST_0));//set 0
            LabelNode endLabelNode = new LabelNode();
            mn.instructions.add(new JumpInsnNode(Opcodes.GOTO, endLabelNode));//end
            mn.instructions.add(trueLabelNode);//If not 0 (is true)
            mn.instructions.add(new InsnNode(Opcodes.ICONST_1));//set 1
            mn.instructions.add(endLabelNode);//end /
            //Convert Z to I, multiply...
            mn.instructions.add(new InsnNode(Opcodes.ICONST_1));//set 1 (int)
            mn.instructions.add(new InsnNode(Opcodes.IMUL));
        }else*/ {
            ASTUtils.error(node, "Operator not recognized.");
        }
    }

    private String getLocalClassCanonicalName(){
      return cn.name;
    }

    private String getLocalClassName(){
      return getLocalClassCanonicalName().replace('.', '/');
    }

    private boolean isLocalDeclaration(IdentifierExpression id){
        return isLocalDeclaration(id.getIdentifier());
    }

    //Find if the local declaration has been done or not.
    private boolean isLocalDeclaration(String id){
        boolean local = false;
        for(int i = this.declarations.size()-1; i>=0; i--){
            if(this.declarations.get(i).equals(id)){
              local = true;
              break;
            }
        }
        return local;
    }

}
