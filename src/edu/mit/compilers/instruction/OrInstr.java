package edu.mit.compilers.instruction;

public class OrInstr extends Instruction {

    private String a;
    private String b;
    
    public OrInstr(String label, String a, String b) {
        super(label);
        this.a = a;
        this.b = b;
    }
    
    public OrInstr(String a, String b) {
        this(null, a, b);
    }
    
    @Override
    public String getInstruction() {
        return "orq\t" + a + ", " + b;
    }

}
