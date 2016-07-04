package edu.mit.compilers.instruction;

public class NegInstr extends Instruction {
    private String value;
    
    public NegInstr(String label, String value) {
        super(label);
        this.value = value;
    }
    
    @Override
    public String getInstruction() {
        return "negq\t" + value;
    }
    
}
