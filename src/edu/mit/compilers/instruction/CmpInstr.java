package edu.mit.compilers.instruction;

/**
 * cmp src, dest - Set flags corresponding to whether dest is less than, equal to, or greater than src
 */
public class CmpInstr extends Instruction {

    private String a;
    private String b;

    public CmpInstr(String label, String a, String b) {
        super(label);
        this.a = a;
        this.b = b;
    }
    
    public CmpInstr(String a, String b) {
        this(null, a, b);
    }

    @Override
    public String getInstruction() {
        return "cmpq\t" + a + ", " + b;
    }
}
