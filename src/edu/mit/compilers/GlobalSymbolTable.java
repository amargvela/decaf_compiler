package edu.mit.compilers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GlobalSymbolTable extends FieldSymbolTable<LocalDescriptor> {
    
    GlobalSymbolTable() {
        this(new HashSet<String>());
    }

    GlobalSymbolTable(Set<String> symbolSet) {
        super(symbolSet);
    }

    @Override
    public void add(String symbol, LocalDescriptor descriptor) {
        super.add(symbol, descriptor);
        descriptor.makeGlobal();
    }
    
    @Override
    public int getStackOffset() {
        return 0;
    }
    
    public List<LocalDescriptor> getAllDescriptors() {
        List<LocalDescriptor> res = new ArrayList<>();
        for (String name : symbols.keySet()) {
            res.add(symbols.get(name));
        }
        return res;
    }
}
