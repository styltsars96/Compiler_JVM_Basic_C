package symbol;

import java.util.Collection;

/**
 * Symbol table
 *
 * @param <E> The type of objects that can be stored in the symbol table.
 */
public interface SymTable<E> {

    /**
     * Lookup a symbol related to a VARIABLE in the symbol table.
     * Also looks for symbol in connected symbol tables!
     *
     * @param s The name of the symbol
     * @return The entry for the symbol or null if not found.
     */
    public E lookup(String s);

    /**
     * Lookup a symbol related to a VARIABLE in the symbol table.
     *
     * @param s The name of the symbol
     * @return The entry for the symbol or null if not found.
     */
    public E lookupOnlyInTop(String s);

    /**
     * Lookup a symbol in the symbol table, for function or variable.
     * Also looks for symbol in connected symbol tables!
     *
     * @param s The name of the symbol
     * @param isFunction true if looking for function, false for variable.
     * @return The entry for the symbol or null if not found.
     */
    public E lookup(String s, boolean isFunction);

    /**
     * Lookup a symbol in the symbol table, for function or variable.
     *
     * @param s The name of the symbol
     * @param isFunction true if looking for function, false for variable.
     * @return The entry for the symbol or null if not found.
     */
    public E lookupOnlyInTop(String s, boolean isFunction);

    /**
     * Add a new symbol table entry for some VARIABLE.
     *
     * @param s The name of the new entry
     * @param symbol The actual entry
     */
    public void put(String s, E symbol);

    /**
     * Add a new symbol table entry.
     *
     * @param s The name of the new entry
     * @param symbol The actual entry
     * @param isFunction true if entry is function.
     */
    public void put(String s, E symbol, boolean isFunction);

    /**
     * Get ALL the symbols available in this symbol table.
     * It also returns the symbols available in the connected symbol tables!
     *
     * @return A collection of symbols.
     */
    public Collection<E> getSymbols();

    /**
     * Get symbols available in this symbol table.
     * It also returns the symbols available in the connected symbol tables!
     * Specify if only FUNCTIONS OR VARIABLES are to be searched.
     *
     * @param isFunction false for variables, true for functions.
     * @return A collection of symbols.
     */
    public Collection<E> getSymbols(boolean isFunction);

    /**
     * Get all the symbols available in ONLY THIS symbol table.
     *
     * @return A collection of symbols.
     */
    public Collection<E> getSymbolsOnlyInTop();

    /**
     * Get all the symbols available in ONLY THIS symbol table.
     * Specify if only FUNCTIONS OR VARIABLES are to be searched.
     *
     * @param isFunction false for variables, true for functions.
     * @return A collection of symbols.
     */
    public Collection<E> getSymbolsOnlyInTop(boolean isFunction);

    /**
     * Clear all symbol entries in the current symbol table.
     */
    public void clearOnlyInTop();

    /**
     * Lookup a symbol related to a VARIABLE in the symbol table.
     * Find only the one in the global context.
     *
     * @param s The name of the symbol
     * @return The entry for the symbol or null if not found.
     */
    public E lookupOnlyInBottom(String s);

    /**
     * Lookup a symbol in the symbol table, for function or variable.
     * Find only the one in the global context.
     *
     * @param s The name of the symbol
     * @param isFunction true if looking for function, false for variable.
     * @return The entry for the symbol or null if not found.
     */
    public E lookupOnlyInBottom(String s, boolean isFunction);

    /**
     * Get the next symbol table.
     *
     * @return The next symbol table.
     */
    public SymTable<E> getNext();

}
