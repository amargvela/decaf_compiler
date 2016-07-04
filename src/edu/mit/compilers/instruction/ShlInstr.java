package edu.mit.compilers.instruction;

/**
 * shl reg - Shift reg to the left by value in cl (low 8 bits of rcx).
 */
public class ShlInstr extends Instruction {

    private String a;

    public ShlInstr(String label, String a) {
        super(label);
        this.a = a;
    }

    @Override
    public String getInstruction() {
        return "shl\t" + a;
    }
}
