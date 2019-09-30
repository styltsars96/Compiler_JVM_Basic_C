package threeaddr;

import org.objectweb.asm.Type;

public class ArrayInitInstr implements Instruction {

    private Integer size;
    private String dynSize;
    private String result;
    private Type type;

    public ArrayInitInstr(Integer size, Type type, String result) {
        this.size = size;
        this.result = result;
        setType(type);
    }

    public ArrayInitInstr(String dynSize, Type type, String result) {
        this.dynSize = dynSize;
        this.result = result;
        setType(type);
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getDynSize() {
        return this.dynSize;
    }

    public void setDynSize(String size) {
        this.dynSize = size;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = Type.getType(type.getDescriptor().replace("[", ""));
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    /**
    * Get size of the primitive Type in bytes.
    *
    */
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

    @Override
    public String emit() {
        return result + " =newArray "+getTypeSize()+" x "+size ;
    }

}
