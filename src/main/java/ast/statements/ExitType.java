package ast.statements;

public enum ExitType {
    BREAK("BREAK"),
    CONTINUE("CONTINUE"),
    RETURN("RETURN");

    private String type;

    private ExitType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type;
    }
}
