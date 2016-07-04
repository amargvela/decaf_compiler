package edu.mit.compilers.instruction;

/**
 * leaq src, dest - The LEA instruction computes a memory address using the same arithmetic that a MOV
 * instruction uses. But unlike the MOV instruction, the LEA instruction just stores the computed address
 * in its target register, instead of loading the contents of that address and storing it.
 */
public class LeaInstr extends Instruction {

    private String a;
    private String b;

    public LeaInstr(String label, String a, String b) {
        super(label);
        this.a = a;
        this.b = b;
    }

    @Override
    public String getInstruction() {
        return "leaq\t" + a + ", " + b;
    }
}
