package symbol;

//To hold Function and Variable with same name.
public class TableEntryWrapper<E>{
    private E function;
    private E variable;

    public TableEntryWrapper(){
    }

    public TableEntryWrapper(E entry, boolean isFunction){
        System.out.println("New table entry:"+ entry + " , isFunction: "+isFunction);
        if (!isFunction) setVariable(entry);
        if ( isFunction)  setFunction(entry);
    }

    public void setFunction(E function){
        this.function = function;
    }

    public E getFunction(){
        return this.function;
    }

    public void setVariable(E variable){
        this.variable = variable;
    }

    public E getVariable(){
        return this.variable;
    }

}
