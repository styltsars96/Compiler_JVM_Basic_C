package threeaddr;

public class ReturnInstr implements  Instruction {

    private String expr;

    public ReturnInstr() {
        expr="";
    }

    public ReturnInstr(String expr){
        this.expr=expr;
    }

    public String getExpr() {
        return expr;
    }

    public void setExpr(String expr) {
        this.expr = expr;
    }

    @Override
    public String emit() {
        return "return "+expr;
    }
}
