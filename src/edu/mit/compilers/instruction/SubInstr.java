package edu.mit.compilers.instruction;

/**
 * sub src, dest - Subtract src from dest.
 */
public class SubInstr extends Instruction {

    private String a;
    private String b;

    public SubInstr(String label, String a, String b) {
        super(label);
        this.a = a;
        this.b = b;
    }
    
    public SubInstr(String a, String b) {
        this(null, a, b);
    }

    @Override
    public String getInstruction() {
        return "subq\t" + a + ", " + b;
    }
}
