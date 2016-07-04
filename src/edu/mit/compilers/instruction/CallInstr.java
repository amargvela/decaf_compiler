package edu.mit.compilers.instruction;

/**
 * call target - Jump unconditionally to target, but also push return address in stack
 */
public class CallInstr extends Instruction {

    private String address;

    public CallInstr(String label, String address) {
        super(label);
        this.address = address;
    }
    
    public CallInstr(String address) {
        this(null, address);
    }

    @Override
    public String getInstruction() {        return "call\t" + address;
    }
}
