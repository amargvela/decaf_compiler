package edu.mit.compilers;

import java.util.ArrayList;
import java.util.List;

import edu.mit.compilers.utils.Enums;

public class MethodDescriptor extends Descriptor {

    private String address;
    private Enums.Type type;
    private List<Enums.Type> parameterTypes;
    private List<String> parameterNames;
    private FieldSymbolTable<LocalDescriptor> paramSymbolTable;

    public MethodDescriptor(
            String name,
            Enums.Type type,
            List<String> parameterNames,
            List<Enums.Type> parameterTypes,
            FieldSymbolTable<LocalDescriptor> paramSymbolTable) {
        super(name);
        this.address = name;
        this.type = type;
        this.parameterNames = parameterNames;
        this.parameterTypes = parameterTypes;
        this.paramSymbolTable = paramSymbolTable;
    }

    public Enums.Type getType() {
        return type;
    }

    public List<Enums.Type> getParameterTypes() {
        return new ArrayList<Enums.Type>(parameterTypes);
    }

    public List<String> getParameterNames() {
        return new ArrayList<String>(parameterNames);
    }

    public FieldSymbolTable<LocalDescriptor> getParamSymbolTable() {
        return paramSymbolTable;
    }
    
    @Override
    public String getAddress() {
        return address;
    }
}
