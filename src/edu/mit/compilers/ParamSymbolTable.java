package edu.mit.compilers;

import java.util.HashSet;
import java.util.Set;

public class ParamSymbolTable extends FieldSymbolTable<LocalDescriptor> {
    private int paramOffset = 16; // parameters start from here.
    
    public ParamSymbolTable() {
        this(new HashSet<String>());
    }
    
    public ParamSymbolTable(Set<String> symbolSet) {
        super(symbolSet);
    }
    
    @Override
    public void add(String symbol, LocalDescriptor descriptor) {
        super.add(symbol, descriptor);
        descriptor.makeParamInStack(paramOffset);
        paramOffset += descriptor.getNBytes();
    }
    
    @Override
    public int getStackOffset() {
        return 0;
    }
}
