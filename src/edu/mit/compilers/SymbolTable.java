package edu.mit.compilers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SymbolTable<D extends Descriptor> {
    protected Map<String, D> symbols;
    protected Set<String> symbolSet;

    SymbolTable() {
        this(new HashSet<String>());
    }

    SymbolTable(Set<String> symbolSet) {
        this.symbolSet = symbolSet;
        this.symbols = new HashMap<>();
    }

    public void add(String symbol, D descriptor) {
        if (canAdd(symbol)) {
            symbols.put(symbol, descriptor);
            symbolSet.add(symbol);
        } else {
            throw new IllegalStateException("This symbol can\'t be added");
        }
    }

    public D get(String symbol) {
        return symbols.get(symbol);
    }

    public boolean canAdd(String symbol) {
        return !symbolSet.contains(symbol);
    }

    public boolean exists(String name) {
        return symbols.containsKey(name);
    }

    public Set<String> getSymbolSetRef() {
        return symbolSet;
    }

    @Override
    public String toString() {
        return "Set: " + symbolSet.toString();
    }
}
