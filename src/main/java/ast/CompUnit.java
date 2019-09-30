package ast;

import java.util.ArrayList;
import java.util.List;

public class CompUnit extends ASTNode {

    private List<Declaration> declarations;

    public CompUnit() {
        declarations = new ArrayList<Declaration>();
    }

    public CompUnit(List<Declaration> declarations) {
        this.declarations = declarations;
    }

    public List<Declaration> getDeclarations() {
        return declarations;
    }

    public void setDeclarations(List<Declaration> declarations) {
        this.declarations = declarations;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
