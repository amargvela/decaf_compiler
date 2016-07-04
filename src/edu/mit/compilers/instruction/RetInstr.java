package edu.mit.compilers.instruction;

/**
 * ret - Pop the return address off the stack and jump unconditionally to this address.
 */
public class RetInstr extends Instruction {

    public RetInstr() {
        super(null);
    }

    @Override
    public String getInstruction() {
        return "ret";
    }
}
