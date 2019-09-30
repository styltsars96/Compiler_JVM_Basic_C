package ast.statements;

import ast.ASTVisitor;
import ast.ASTVisitorException;
import ast.Declaration;
import org.objectweb.asm.Type;

public class ParameterDeclaration extends Declaration {

    private boolean isArray;
    private ast.exprs.IdentifierExpression id;
    private Type type;

    public ParameterDeclaration(boolean isArray, ast.exprs.IdentifierExpression id, Type type) {
        this.isArray = isArray;
        if(isArray && !type.getDescriptor().contains("[")){
            System.out.println("Changed type of "+ id.getIdentifier() +" to array of " + type.getDescriptor() );//DEBUG
            this.type = Type.getType('['+type.getDescriptor());
        }else this.type = type;
        this.id = id;

    }

    public ParameterDeclaration(ast.exprs.IdentifierExpression id, Type type) {
        this.isArray =  type.getDescriptor().contains("[");
        this.id = id;
        this.type = type;
    }

    public boolean getIsArray() {
        return isArray;
    }

    @Override
    public ast.exprs.IdentifierExpression getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public void setIsArray(boolean isArray) {
        this.isArray = isArray;
    }

    public void setId(ast.exprs.IdentifierExpression id) {
        this.id = id;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
