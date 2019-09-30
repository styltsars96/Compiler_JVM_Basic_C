/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package threeaddr;

import ast.Operator;

public class CondJumpInstr extends GotoInstr {

    private Operator op;
    private String arg1;
    private String arg2;

    public CondJumpInstr(Operator op, String arg1) {
        this(op, arg1, null, null);
    }

    public CondJumpInstr(Operator op, String arg1, LabelInstr target) {
        this(op, arg1, null, target);
    }

    public CondJumpInstr(Operator op, String arg1, String arg2) {
        this(op, arg1, arg2, null);
    }

    public CondJumpInstr(Operator op, String arg1, String arg2, LabelInstr target) {
        super(target);
        this.op = op;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    public Operator getOp() {
        return op;
    }

    public void setOp(Operator op) {
        this.op = op;
    }

    public String getArg1() {
        return arg1;
    }

    public void setArg1(String arg1) {
        this.arg1 = arg1;
    }

    public String getArg2() {
        return arg2;
    }

    public void setArg2(String arg2) {
        this.arg2 = arg2;
    }

    @Override
    public String emit() {
        String temp = null;
        if(arg2==null){
          temp = "if " + op + " " + arg1;
        }else{
          temp = "if " + arg1 + " " + op + " " + arg2;
        }
        if (target == null) {
            return  temp + " goto _";
        } else {
            return temp + " goto " + target.getName();
        }
    }

}
