package ast.statements;

import ast.ASTVisitor;
import ast.ASTVisitorException;
import ast.Declaration;
import org.objectweb.asm.Type;
import ast.exprs.IdentifierExpression;

public class VariableDeclaration extends Declaration {
    private boolean isArray;
    private IdentifierExpression id;
    private Type type;

    public VariableDeclaration(boolean isArray, IdentifierExpression id, Type type) {
      this.isArray = isArray;
      if(isArray && !type.getDescriptor().contains("[")){
          System.out.println("Changed type of "+ id.getIdentifier() +" to array of " + type.getDescriptor() );//DEBUG
          this.type = Type.getType('['+type.getDescriptor());
      }else this.type = type;
      this.id = id;
    }

    public VariableDeclaration(IdentifierExpression id, Type type) {
        this.id = id;
        this.type = type;
        this.isArray =  type.getDescriptor().contains("[");
    }

    public boolean getIsArray() {
        return isArray;
    }

    @Override
    public IdentifierExpression getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public void setIsArray(boolean isArray) {
        this.isArray = isArray;
    }

    public void setId(IdentifierExpression id) {
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
