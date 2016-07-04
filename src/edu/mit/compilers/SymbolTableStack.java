package edu.mit.compilers;

import java.util.LinkedList;
import java.util.List;

public class SymbolTableStack {
    
    private List<FieldSymbolTable<LocalDescriptor>> symbolTables;

    public SymbolTableStack() {
        symbolTables = new LinkedList<>();
    }
    
    public void push(FieldSymbolTable<LocalDescriptor> symbolTable) {
        symbolTables.add(0, symbolTable);
    }
    
    public FieldSymbolTable<LocalDescriptor> top() {
        return symbolTables.get(0);
    }
    
    public FieldSymbolTable<LocalDescriptor> pop() {
        FieldSymbolTable<LocalDescriptor> table = symbolTables.get(0);
        symbolTables.remove(0);
        return table;
    }
    
    public LocalDescriptor getDescriptor(String symbol) {
        for (FieldSymbolTable<LocalDescriptor> table : symbolTables) {
            if (table.exists(symbol)) {
                return table.get(symbol);
            }
        }
        return null;
    }
}
