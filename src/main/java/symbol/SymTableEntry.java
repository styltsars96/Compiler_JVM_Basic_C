package symbol;

import org.objectweb.asm.Type;

public class SymTableEntry {

    private String id;
    private Type type;//if function then it is a composite ASM type
    private boolean isFunction;//false if Variable, false by default!
    private int arraySize;//0 if not array, >0 if array, -1 if dynamically allocated size.
    private Integer index;//if negative, global variable. NEW

    public SymTableEntry(String id, Type type, Integer index) {
        //Also checks if it is a function, and sets the appropriate value!
        this(id, type, ( type.toString().indexOf('(') >= 0 ), index );
    }

    public SymTableEntry(String id, Type type, boolean isFunction,Integer index) {
        this.id = id;
        this.type = type;
        this.isFunction = isFunction;
        this.arraySize = 0;
        this.index=index;
    }

    /**
     * Symbol table entry for ARRAY!
     */
    public SymTableEntry(String id, Type type, int arraySize, Integer index) {
        this.id = id;
        this.type = type;
        this.isFunction = false;
        this.arraySize = arraySize;
        this.index=index;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        if(this.type==null ? ( this.isFunction && type.toString().indexOf('(') < 0) : false ){
            //Special case: Indicate that it is a Function beforehand, and pass the bare RVT
            this.type = Type.getMethodType(type);
        }else{
            this.type = type;
        }
    }

    /**
     * @param isFunction set true if Function, false if Variable
     */
    public void setIsFunction(boolean isFunction) {
    	 this.isFunction = isFunction;
    }

    /**
     * @return false if Variable, true if Function
     */
    public boolean getIsFunction(){
        return this.isFunction;
    }

    /**
     * @param arraySize Size of array (0 is NOT ARRAY)
     */
    public void setArraySize(int arraySize) {
        if(this.isFunction==true) return;
        this.arraySize = arraySize;
    }

    /**
     * @return size of array, 0 if not array!
     */
    public Integer getArraySize(){
        return this.arraySize;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    /**
     * Custom code, instead of actual Objects hashcode.
     * Used for the benefit of HashTables and HashMaps.
     *
     * @return A custom hash code based on id and type input.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 97 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SymTableEntry other = (SymTableEntry) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        if (this.type != other.type && (this.type == null || !this.type.equals(other.type))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
      return this.id + " Type:" + this.type + " ArraySize:"  + this.arraySize + " isFunction:" + this.isFunction;
    }

}
