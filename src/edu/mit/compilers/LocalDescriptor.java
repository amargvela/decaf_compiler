package edu.mit.compilers;

import java.util.HashSet;
import java.util.Set;

import edu.mit.compilers.utils.Address;
import edu.mit.compilers.utils.Assertion;
import edu.mit.compilers.utils.Consts;
import edu.mit.compilers.utils.Enums;
import edu.mit.compilers.utils.Enums.FieldType;

public class LocalDescriptor extends Descriptor {

    private Enums.Type type;
    private int length;
    protected String symbolicValue;
    protected String variableId;
    
    static protected int symbolCounter = 0;
    static protected int variableCounter = 0;
    static protected Set<String> allVariables = new HashSet<String>();
    
    private Enums.FieldType fieldType = null;
    private Integer startAddress = null;
    private Integer paramIndex = null;

    public LocalDescriptor(String name, Enums.Type type, int length) {
        super(name);
        this.type = type;
        this.length = length;
        this.symbolicValue = "v_" + LocalDescriptor.symbolCounter++;
        this.variableId = name + "_" + variableCounter++;
        allVariables.add(variableId);
    }

    // Constructor for simple classes (non-array)
    public LocalDescriptor(String name, Enums.Type type) {
        super(name);
        if (type == Enums.Type.EXPR) {
            throw new IllegalArgumentException("LocalDescriptor: Type can\'t be EXPR");
        }
        this.type = type;
        this.length = 0;
        this.symbolicValue = "v_" + LocalDescriptor.symbolCounter++;
        this.variableId = name + "_" + variableCounter++;
    }

    public static Set<String> getAllVariables() {
        return allVariables;
    }
    
    public Enums.Type getType() {
        return type;
    }
    
    public int getLength() {
        return length;
    }
    
    public boolean isArray() {
        return length > 0;
    }
    
    public void makeGlobal() {
        Assertion.check(fieldType == null);

        fieldType = Enums.FieldType.GLOBAL;
    }
    
    public void makeLocal(int startAddress) {
        Assertion.check(fieldType == null);
        fieldType = Enums.FieldType.LOCAL;
        this.startAddress = startAddress;
    }
    
    public void makeParamInStack(int startAddress) {
        Assertion.check(fieldType == null);
        
        fieldType = Enums.FieldType.PARAM;
        this.startAddress = startAddress;
    }
    
    public void makeParamInRegister(int index) {
        Assertion.check(fieldType == null);
        
        fieldType = Enums.FieldType.PARAM;
        this.paramIndex = index;
    }
    
    public int getNBytes() {
        int nUnits = length;
        if (!isArray()) {
            nUnits = 1;
        }
        
        return Consts.getUnitSize(type) * nUnits;
    }
    
    public boolean inStack() {
        return (fieldType == FieldType.LOCAL ||
            fieldType == FieldType.PARAM && startAddress != null);
    }
    
    public boolean isGlobal() {
        return fieldType == FieldType.GLOBAL;
    }

    @Override
    public String getAddress(String index) {
        Assertion.check(isArray());
        
        switch (fieldType) {
        case GLOBAL:
            return Address.getGlobalArray(name, index);
        case LOCAL:
            return Address.getLocalArray(startAddress, index);
        case PARAM:
            throw new RuntimeException("Parameters cannot be arrays");
        default:
            throw new RuntimeException("Unimplemented for field type " + fieldType);
        }
    }
    
    @Override
    public String getAddress() {
        Assertion.check(!isArray());

        switch (fieldType) {
        case GLOBAL:
            return Address.getGlobal(name);
        case LOCAL:
            return Address.getLocal(startAddress);
        case PARAM:
            if (startAddress == null) {
                return Address.getParam(paramIndex);
            } else {
                return Address.getLocal(startAddress);
            }
        default:
            throw new RuntimeException("Unimplemented for field type " + fieldType);
        }
    }

    public String getVariableId() {
        return variableId;
    }

    public String getSymbolicValue() {
        return symbolicValue;
    }

    public void setSymbolicValue() {
        LocalDescriptor.symbolCounter++;
        this.symbolicValue = "v_" + LocalDescriptor.symbolCounter;
    }

    public static void resetVariableCounter() {
        variableCounter = 0;
    }
}
