package edu.mit.compilers.instruction;

public class LabelInstr extends Instruction {
    
    public LabelInstr(String label) {
        super(label);
    }

    @Override
    public String getInstruction() {
        return "";
    }
    
    @Override
    public String toString() {
        return label + ":\n";
    }
}
