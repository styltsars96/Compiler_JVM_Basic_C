package symbol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashSymTable<E> implements SymTable<E> {

    private final Map<String, TableEntryWrapper<E>> table = new HashMap<String, TableEntryWrapper<E>>();
    private SymTable<E> nextSymTable;

    public HashSymTable() {
        this(null);
    }

    public HashSymTable(SymTable<E> nextSymTable) {
        this.nextSymTable = nextSymTable;
    }

    @Override
    public E lookup(String s) {//lookup variables only
        return lookup(s,false);
    }

    @Override
    public E lookupOnlyInTop(String s) {//lookup variables only
        return lookupOnlyInTop(s, false);
    }

    @Override
    public E lookup(String s, boolean isFunction) {//lookup function if true
      TableEntryWrapper<E> r = table.get(s);
      if (r != null) {//If a wrapper with the name is found
          if(!isFunction && r.getVariable() != null)//If variable is found
              return r.getVariable();//Return it.

          if(isFunction && r.getFunction() != null)//If function is found
            return r.getFunction();//Return it.
      }
      if (nextSymTable != null) {//If nothing is found
          return nextSymTable.lookup(s, isFunction);//check next
      }
      return null;
    }

    @Override
    public E lookupOnlyInTop(String s, boolean isFunction) {//lookup function if true
        TableEntryWrapper<E> r = table.get(s);
        if (r != null) return isFunction ? r.getFunction() : r.getVariable() ;
        return null;
    }

    @Override
    public void put(String s, E symbol) {//put variable
        put(s, symbol, false);
    }

    @Override
    public void put(String s, E symbol, boolean isFunction){//put a function
        if(table.get(s)==null){//Instantiate entry wrapper if it doesn't exist.
            table.put(s, new TableEntryWrapper<E>(symbol, isFunction));
        }else{
            TableEntryWrapper<E> temp = table.get(s);
            if(isFunction){
                temp.setFunction(symbol);
            }else{
                temp.setVariable(symbol) ;
            }
        }
    }

    @Override
    public Collection<E> getSymbols() {
      List<E> symbols = new ArrayList<E>();
      Collection<TableEntryWrapper<E>> tableValues = table.values();
      E temp;
      for(TableEntryWrapper<E> tableEntry : tableValues){
        temp = tableEntry.getVariable();
        if(temp!=null) symbols.add(temp);
        temp = tableEntry.getFunction();
        if(temp!=null) symbols.add(temp);
      }
      if (nextSymTable != null) {
        //System.out.println("Table entries before next:" + symbols); //DEBUG
        symbols.addAll(nextSymTable.getSymbols());
      }
      return symbols;
    }

    @Override
    public Collection<E> getSymbols(boolean isFunction){
      List<E> symbols = new ArrayList<E>();
      Collection<TableEntryWrapper<E>> tableValues = table.values();
      E temp;
      for(TableEntryWrapper<E> tableEntry : tableValues){
          if(isFunction == true){
              temp = tableEntry.getFunction();
              if(temp!=null) symbols.add(temp);
          }else if(isFunction == false){
              temp = tableEntry.getVariable();
              if(temp!=null) symbols.add(temp);
          }
      }
      if (nextSymTable != null) {
          symbols.addAll(nextSymTable.getSymbols(isFunction));
      }
      return symbols;
    }

    @Override
    public Collection<E> getSymbolsOnlyInTop() {
      List<E> symbols = new ArrayList<E>();
      Collection<TableEntryWrapper<E>> tableValues = table.values();
      E temp;
      for(TableEntryWrapper<E> tableEntry : tableValues){
        temp = tableEntry.getVariable();
        if(temp!=null) symbols.add(temp);
        temp = tableEntry.getFunction();
        if(temp!=null) symbols.add(temp);
      }
      return symbols;
    }

    @Override
    public Collection<E> getSymbolsOnlyInTop(boolean isFunction){
      List<E> symbols = new ArrayList<E>();
      Collection<TableEntryWrapper<E>> tableValues = table.values();
      E temp;
      for(TableEntryWrapper<E> tableEntry : tableValues){
          if(isFunction == true){
              temp = tableEntry.getFunction();
              if(temp!=null) symbols.add(temp);
          }else if(isFunction == false){
              temp = tableEntry.getVariable();
              if(temp!=null) symbols.add(temp);
          }
      }
      return symbols;
    }

    @Override
    public void clearOnlyInTop() {
        table.clear();
    }

    @Override
    public SymTable<E> getNext() {
        return this.nextSymTable;
    }

    @Override
    public E lookupOnlyInBottom(String s){
        return lookupOnlyInBottom(s, false);
    }

    @Override
    public E lookupOnlyInBottom(String s, boolean isFunction){
        SymTable<E> bottom = this.getNext();
        while(bottom.getNext() != null){
            bottom = bottom.getNext();
        }//Here we surely found the bottom.
        System.out.println("Bottom has:\n"+bottom);
        return bottom.lookup(s, isFunction);
    }

    @Override
    public String toString(){
        return table.keySet().toString();
    }


}
