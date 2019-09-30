package threeaddr;

import org.objectweb.asm.Type;

public class ArrIndAccessInstr implements  Instruction {

    private String ArrayName;
    private Type type;
    private int index;
    private String dynIndex;
    private String target;

    public ArrIndAccessInstr(String arrayName, Type type, int index, String target) {
        ArrayName = arrayName;
        setType(type);
        this.index = index;
        this.target = target;
    }

    public ArrIndAccessInstr(String arrayName, Type type, String dynIndex, String target) {
        ArrayName = arrayName;
        setType(type);
        this.dynIndex = dynIndex;
        this.target = target;
    }

    public Integer getTypeSize(){
        Integer ret = 0;
        switch(this.type.getSize()){
            case 2:
                ret = 8;
                break;
            case 1:
                if(this.type.equals(Type.CHAR_TYPE)){
                    ret = 2;
                }else ret = 4;
                break;
        }

        return ret;
    }

    public String getArrayName() {
        return ArrayName;
    }

    public void setArrayName(String arrayName) {
        ArrayName = arrayName;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = Type.getType(type.getDescriptor().replace("[", ""));
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getDynIndex() {
        return this.dynIndex;
    }

    public void setDynIndex(String index) {
        this.dynIndex = index;
    }

    public String getArg1() {
        return target;
    }

    public void setArg1(String target) {
        this.target = target;
    }

    @Override
    public String emit() {
        //ALT: "["+getTypeSize()+" X "+index+"]"
        if (dynIndex == null)
          return target+" = "+ArrayName+"["+index+"]";
        return target+" = "+ArrayName+"["+dynIndex+"]";
    }
}
