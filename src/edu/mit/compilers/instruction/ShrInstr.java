package edu.mit.compilers.instruction;

/**
 * shr reg - Shift reg to the right by value in cl (low 8 bits of rcx).
 */
public class ShrInstr extends Instruction {

    private String a;

    public ShrInstr(String label, String a) {
        super(label);
        this.a = a;
    }

    @Override
    public String getInstruction() {
        return "shr\t" + a;
    }
}
