package ast.statements.assign;

import ast.Expression;
import ast.ASTVisitor;
import ast.ASTVisitorException;
import ast.Assignment;
import org.objectweb.asm.Type;
import ast.exprs.IdentifierExpression;


public class ArrayInitAssignment extends Assignment{
    //This if for initialization of an array.
    private Type type;//ASM type of array.
    //Types of size...
    private Integer ArraySize;//Size when a int literal is given.
    private Expression sizeExpression;//When an expression is given.

    public ArrayInitAssignment(IdentifierExpression id,Type type, int ArraySize) {
        this.id = id;
        this.type=Type.getType('['+type.getDescriptor());
        this.ArraySize = ArraySize;
        this.sizeExpression = null;
    }

    public ArrayInitAssignment(IdentifierExpression id,Type type, Expression sizeExpression) {
        this.id = id;
        this.type=Type.getType('['+type.getDescriptor());
        this.ArraySize = null;
        this.sizeExpression = sizeExpression;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Integer getArraySize() {
        return ArraySize;
    }

    public void setArraySize(Integer ArraySize) {
        this.ArraySize = ArraySize;
    }

    public Expression getSizeExpression() {
        return this.sizeExpression;
    }

    public void setSizeExpression(Expression sizeExpression) {
        this.sizeExpression = sizeExpression;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
}
