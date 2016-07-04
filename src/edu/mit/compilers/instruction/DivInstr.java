package edu.mit.compilers.instruction;

public class DivInstr extends Instruction {

    private String divisor;
    
    public DivInstr(String label, String divisor) {
        super(label);
        this.divisor = divisor;
    }
    
    public DivInstr(String divisor) {
        this(null, divisor);
    }
    
    @Override
    public String getInstruction() {
        return "cqto\n\t" + "idivq\t" + divisor;
    }
}
