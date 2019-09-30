/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.statements;

import ast.ASTVisitor;
import ast.ASTVisitorException;
import ast.Statement;
import java.util.List;


public class CompoundStatement extends Statement{
    private List<Statement> StmtList;

    public CompoundStatement(List<Statement> StmtList) {
        this.StmtList = StmtList;
    }

    public List<Statement> getStmtList() {
        return StmtList;
    }

    public void setStmtList(List<Statement> StmtList) {
        this.StmtList = StmtList;
    }
       
    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
