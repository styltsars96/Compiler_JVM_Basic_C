package threeaddr;

import org.objectweb.asm.Type;

public class ArrIndAssignInstr implements  Instruction {

    private String TableName;
    private Type type;
    private int index;
    private String dynIndex;
    private String arg1;

    public ArrIndAssignInstr(String tableName, Type type, int index, String arg1) {
        TableName = tableName;
        setType(type);
        this.index = index;
        this.arg1 = arg1;
    }

    public ArrIndAssignInstr(String tableName, Type type, String dynIndex, String arg1) {
        TableName = tableName;
        setType(type);
        this.dynIndex = dynIndex;
        this.arg1 = arg1;
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

    public String getTableName() {
        return TableName;
    }

    public void setTableName(String tableName) {
        TableName = tableName;
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
        return arg1;
    }

    public void setArg1(String arg1) {
        this.arg1 = arg1;
    }

    @Override
    public String emit() {
        //return TableName+"["+getTypeSize()+" X "+index+"] = "+arg1;
        if (dynIndex == null)
          return TableName+"["+index+"] = "+arg1;
        return TableName+"["+dynIndex+"] = "+arg1;
    }
}
