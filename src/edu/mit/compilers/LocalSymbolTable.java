package edu.mit.compilers;

import java.util.HashSet;
import java.util.Set;

public class LocalSymbolTable extends FieldSymbolTable<LocalDescriptor> {
    
    public LocalSymbolTable(int initialStackOffset) {
        this(new HashSet<String>(), initialStackOffset);
    }
    
    public LocalSymbolTable(Set<String> symbolSet, int initialStackOffset) {
        super(symbolSet);
        offset = initialStackOffset;
    }

    @Override
    public void add(String symbol, LocalDescriptor descriptor) {
        super.add(symbol, descriptor);
        offset -= descriptor.getNBytes();
        descriptor.makeLocal(offset);
    }
    
    @Override
    public int getStackOffset() {
        return offset;
    }
}
