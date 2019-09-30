package threeaddr;

public class ParamInstr implements  Instruction {
    private String arg1;

    public ParamInstr(String arg1) {
        this.arg1 = arg1;
    }

    public String getArg1() {
        return arg1;
    }

    public void setArg1(String arg1) {
        this.arg1 = arg1;
    }

    @Override
    public String emit() {
        return "param "+arg1;
    }
}
