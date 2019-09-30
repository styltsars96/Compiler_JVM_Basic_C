package ast.statements;

import ast.ASTVisitor;
import ast.ASTVisitorException;
import ast.Declaration;
import org.objectweb.asm.Type;
import ast.exprs.IdentifierExpression;

import java.util.List;

public class FunctionDeclaration extends Declaration {

    private Type type;
    private IdentifierExpression id;
    private List<ParameterDeclaration> params;
    private CompoundStatement statement;

    public FunctionDeclaration(Type type, IdentifierExpression name, List<ParameterDeclaration> params, CompoundStatement statement) {
        this.type = type;
        this.id = name;
        this.params = params;
        this.statement = statement;
    }

    public Type getType() {
        return type;
    }

    @Override
    public IdentifierExpression getId() {
        return id;
    }

    public List<ParameterDeclaration> getParams() {
        return params;
    }

    public CompoundStatement getStatement() {
        return statement;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setId(IdentifierExpression name) {
        this.id = name;
    }

    public void setParams(List<ParameterDeclaration> params) {
        this.params = params;
    }

    public void setStatement(CompoundStatement statement) {
        this.statement = statement;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
