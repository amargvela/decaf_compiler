package edu.mit.compilers.instruction;

public abstract class Instruction {

    protected String label;

    public Instruction(String label) {
        this.label = label;
    }

    public String getLabel() {
        if (label != null) {
            return label;
        } else {
            throw new RuntimeException("Label does not exist");
        }
    }

    public boolean hasLabel() {
        return label != null;
    }

    abstract public String getInstruction();

    @Override
    public String toString() {
        String result = "";
        if (hasLabel()) {
            result += label + ":\n";
        }
        result += "\t" + getInstruction() + "\n";
        return result;
    }

}
