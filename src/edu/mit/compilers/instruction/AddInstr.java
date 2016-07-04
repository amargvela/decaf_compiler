package edu.mit.compilers.instruction;

/**
 * add src, dest - Add src to dest.
 */
public class AddInstr extends Instruction {

    private String a;
    private String b;

    public AddInstr(String label, String a, String b) {
        super(label);
        this.a = a;
        this.b = b;
    }
    
    public AddInstr(String a, String b) {
        this(null, a, b);
    }

    @Override
    public String getInstruction() {
        return "addq\t" + a + ", " + b;
    }
}
