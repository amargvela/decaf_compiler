package edu.mit.compilers.instruction;

public class MulInstr extends Instruction {

    private String a;
    private String b;
    
    public MulInstr(String label, String a, String b) {
        super(label);
        this.a = a;
        this.b = b;
    }
    
    public MulInstr(String a, String b) {
        this(null, a, b);
    }
    
    @Override
    public String getInstruction() {
        return "imulq\t" + a + ", " + b;
    }
}
