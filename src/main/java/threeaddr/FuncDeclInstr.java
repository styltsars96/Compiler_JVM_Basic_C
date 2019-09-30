package threeaddr;

public class FuncDeclInstr implements  Instruction {

    private String name;

    public FuncDeclInstr(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String emit() {
        return "\n--"+name+"--";
    }
}
