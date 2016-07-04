package edu.mit.compilers.instruction;

/**
 * ror src, dest - Rotate dest to the left or right by src bits.
 */
public class RorInstr extends Instruction {

    private String a;
    private String b;

    public RorInstr(String label, String a, String b) {
        super(label);
        this.a = a;
        this.b = b;
    }

    @Override
    public String getInstruction() {
        return "ror\t" + a + ", " + b;
    }
}
