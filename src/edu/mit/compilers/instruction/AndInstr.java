package edu.mit.compilers.instruction;

public class AndInstr extends Instruction {
    private String a;
    private String b;
    
    public AndInstr(String label, String a, String b) {
        super(label);
        this.a = a;
        this.b = b;
    }
    
    public AndInstr(String a, String b) {
        this(null, a, b);
    }

    @Override
    public String getInstruction() {
        return "andq\t" + a + ", " + b;
    }
}
