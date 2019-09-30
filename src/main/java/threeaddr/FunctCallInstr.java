package threeaddr;


public class FunctCallInstr implements Instruction{

    private  String arg1;
    private String res;
    int params;

    public FunctCallInstr(String res,String arg1, int params) {
        this.res = res;
        this.arg1 = arg1;
        this.params = params;
    }

    public FunctCallInstr(String arg1, int params) {
        this.arg1 = arg1;
        this.params = params;
    }

    public String getArg1() {
        return arg1;
    }

    public void setArg1(String arg1) {
        this.arg1 = arg1;
    }

    public int getParams() {
        return params;
    }

    public void setParams(int params) {
        this.params = params;
    }

    @Override
    public String emit() {
        if (res!=null)
        return res+" = call "+arg1+", "+params;
        return "call "+arg1+", "+params;
    }
}
