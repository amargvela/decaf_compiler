package edu.mit.compilers.instruction;

/**
 * pushq src - Decrease %rsp and put src in it
 */
public class PushInstr extends Instruction {

    private String what;
    public PushInstr(String label, String what) {
        super(label);
        this.what = what;
    }
    
    public PushInstr(String what) {
        this(null, what);
    }
    
    @Override
    public String getInstruction() {
        return "pushq\t" + what;
    }
}
