package edu.mit.compilers;

import java.util.HashSet;
import java.util.Set;

public class FieldSymbolTable<D extends Descriptor> extends SymbolTable<D> {
    protected int offset;
    
    public FieldSymbolTable() {
        this(new HashSet<String>());
    }
    
    public FieldSymbolTable(Set<String> symbolSet) {
        super(symbolSet);
    }
    
    public int getStackOffset() {
        throw new RuntimeException("Method getStackOffset is not implemented");
    }

    public void push(int regSize) {
        offset -= regSize;
    }
    
    public void pop(int regSize) {
        offset += regSize;
    }
}
