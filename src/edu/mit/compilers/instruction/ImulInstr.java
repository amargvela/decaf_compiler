package edu.mit.compilers.instruction;

/**
 * imul src, dest - Multiply dest by src.
 */
public class ImulInstr extends Instruction {

    private String a;
    private String b;

    public ImulInstr(String label, String a, String b) {
        super(label);
        this.a = a;
        this.b = b;
    }

    @Override
    public String getInstruction() {
        return "imul\t" + a + ", " + b;
    }
}
