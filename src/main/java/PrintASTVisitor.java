
/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
import ast.*;
import ast.exprs.*;
import ast.statements.*;
import ast.statements.assign.*;


import java.util.List;
import org.apache.commons.lang3.StringEscapeUtils;

public class PrintASTVisitor implements ASTVisitor {

    
    @Override
    public void visit(CompUnit node) throws ASTVisitorException {
        for (Declaration d : node.getDeclarations()) {
            d.accept(this);
        }
    }

    //Declarations
    @Override
    public void visit(VariableDeclaration node) throws ASTVisitorException {
        System.out.print(node.getType() + " ");
        if (node.getIsArray()) {
            System.out.print("[] ");
        }
        node.getId().accept(this);
        System.out.println(";");
    }
    
    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        System.out.print(node.getType() + " ");
        if (node.getIsArray()) {
            System.out.print("[] ");
        }
        node.getId().accept(this);
    }
    
    @Override
    public void visit(FunctionDeclaration node) throws ASTVisitorException {
        System.out.print(node.getType() + " ");
        node.getId().accept(this);
        System.out.print("(");
        if (node.getParams() != null) {//If there is one parameter at least.
            for(int i=0;i<node.getParams().size();i++){
                node.getParams().get(i).accept(this);
                if(i<node.getParams().size()-1){//If this parameter is not the last one.
                    System.out.print(", ");
                }
            }
        }
        System.out.print(")");
        node.getStatement().accept(this);
    }
    
    //Statements
    @Override
    public void visit(PrintStatement node) throws ASTVisitorException {
        System.out.print("print( ");
        node.getExpression().accept(this);
        System.out.println(" );");
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        System.out.print("IF (");
        node.getExpression().accept(this);
        System.out.println(") ");
        node.getStatement().accept(this);
        
    }
    
    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        System.out.print("IF (");
        node.getExpression().accept(this);
        System.out.println(")");
        node.getStatement1().accept(this);
        System.out.println(" ELSE ");
        node.getStatement2().accept(this);
    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        System.out.print("WHILE (");
        node.getExpression().accept(this);
        System.out.println(")");
        node.getStatement().accept(this);
    }
    
    @Override
    public void visit(DoWhileStatement node) throws ASTVisitorException {
        System.out.print("DO ");
        node.getStatement().accept(this);
        System.out.print(" WHILE (");
        node.getExpression().accept(this);
        System.out.println(");");
    }

    @Override
    public void visit(FunctionCallStatement node) throws ASTVisitorException {
        node.getFunct().accept(this);
    }
    
    @Override
    public void visit(ExitStatement node) throws ASTVisitorException {
        if(node.getType().equals(ExitType.RETURN)){
            System.out.print(node.getType()+" ");
            if(node.getExpr()!=null)
            node.getExpr().accept(this);
            System.out.println(";");
        }else{
            System.out.println(node.getType()+" ;");
        }
    }
    
    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        System.out.println(" { ");
        for (Statement st : node.getStmtList()) {
            st.accept(this);
        }
        System.out.println(" } ");
    }

    //Expressions
    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        System.out.print(node.getIdentifier());
    }

    @Override
    public void visit(DoubleLiteralExpression node) throws ASTVisitorException {
        System.out.print(node.getLiteral());
    }

    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        System.out.print(node.getLiteral());
    }
        
    @Override
    public void visit(CharacterLiteralExpression node) throws ASTVisitorException {
         System.out.print("'"+node.getLiteral()+"'");
    }

    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        System.out.print("\"");
        System.out.print(StringEscapeUtils.escapeJava(node.getLiteral()));
        System.out.print("\"");
    }
    
    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        System.out.print(" ");
        System.out.print(node.getOperator());
        System.out.print(" ");
        node.getExpression2().accept(this);
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        System.out.print(node.getOperator());
        System.out.print(" ");
        node.getExpression().accept(this);
    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {
        System.out.print("( ");
        node.getExpression().accept(this);
        System.out.print(" )");
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {
        node.getFunctionName().accept(this);
        System.out.append("(");
        List<Expression> exprs= node.getExpressions();
        for (int i =0; i<exprs.size();i++) {
        	System.out.print(ASTUtils.getType(exprs.get(i))+" ");
            exprs.get(i).accept(this);
            if(i<exprs.size()-1){
                System.out.print(", ");
            }
        }
        System.out.println(");");
    }

    //Assignments
    @Override
    public void visit(BasicAssignment node) throws ASTVisitorException {
        node.getId().accept(this);
        System.out.print(" = ");
        node.getExpr().accept(this);
        System.out.println(";");
    }

    @Override
    public void visit(ArrayInitAssignment node) throws ASTVisitorException {
        node.getId().accept(this);
        System.out.println(" = NEW "+node.getType()+"["+node.getArraySize()+"] ;");
    }

    @Override
    public void visit(ArrayIndexAssignment node) throws ASTVisitorException {
        node.getId().accept(this);
        System.out.print("["+node.getIndex()+"] = ");
        node.getExpr().accept(this);
        System.out.println(";");
    }

    @Override
    public void visit(CompoundAssign node) throws ASTVisitorException {
        if(node.getVardecl() !=null){
            System.out.print(node.getVardecl().getType());
        }
        node.getAssign().accept(this);
    }

}
