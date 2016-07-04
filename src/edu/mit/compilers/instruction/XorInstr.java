package edu.mit.compilers.instruction;

public class XorInstr extends Instruction {
    private String a;
    private String b;

    public XorInstr(String label, String a, String b) {
        super(label);
        this.a = a;
        this.b = b;
    }

    @Override
    public String getInstruction() {
        return "xor\t" + a + ", " + b;
    }
}
