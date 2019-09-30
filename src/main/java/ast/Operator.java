package ast;

import ast.Operator;

public enum Operator {

    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVISION("/"),
    MOD("%"),
    EQUAL("=="),
    NOT_EQUAL("!="),
    GREATER(">"),
    GREATER_EQ(">="),
    LESS("<"),
    LESS_EQ("<="),
    AND("&&"),
    OR("||"),
    NOT("!"),
    IS_TRUE("IS_TRUE"),//for >=3AC phases only
    IS_FALSE("IS_FALSE");//for >=3AC phases only

    private String type;

    private Operator(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
    public boolean isUnary() {
        return this.equals(Operator.MINUS)|| this.equals(Operator.NOT);
    }

    public boolean isRelational() {
        return this.equals(Operator.EQUAL) || this.equals(Operator.NOT_EQUAL)
                || this.equals(Operator.GREATER) || this.equals(Operator.GREATER_EQ)
                || this.equals(Operator.LESS) || this.equals(Operator.LESS_EQ);
    }

    public boolean isBinaryBoolean(){
        return this.equals(Operator.AND) || this.equals(Operator.OR);
    }

    @Override
    public String toString() {
        return type;
    }

}
