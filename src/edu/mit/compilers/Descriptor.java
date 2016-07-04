package edu.mit.compilers;

public abstract class Descriptor {
    protected String name;
    
    public Descriptor(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public String getAddress() {
        throw new RuntimeException("Method getAddress() not implemented");
    }
    
    public String getAddress(String index) {
        throw new RuntimeException("Method getAddress(String index) not implemented");
    }
}
