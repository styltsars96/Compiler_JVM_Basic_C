package ast.statements.assign;

import ast.ASTVisitor;
import ast.ASTVisitorException;
import ast.Assignment;
import ast.Statement;
import ast.statements.VariableDeclaration;

public class CompoundAssign extends Statement {

    private VariableDeclaration vardecl;
    private Assignment assign;

    public CompoundAssign(Assignment assign) {
        this.assign = assign;
    }

    public CompoundAssign(VariableDeclaration vardecl, Assignment assign) {
        this.vardecl = vardecl;
        this.assign = assign;
    }

    public VariableDeclaration getVardecl() {
        return vardecl;
    }

    public void setVardecl(VariableDeclaration vardecl) {
        this.vardecl = vardecl;
    }

    public Assignment getAssign() {
        return assign;
    }

    public void setAssign(Assignment assign) {
        this.assign = assign;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
