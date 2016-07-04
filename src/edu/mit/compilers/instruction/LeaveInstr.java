package edu.mit.compilers.instruction;

/**
 * leave - Removes local variables from the stack frame by restoring the old values of %rsp and %rbp.
 */
public class LeaveInstr extends Instruction {

    public LeaveInstr(String label) {
        super(label);
    }

    @Override
    public String getInstruction() {
        return "leave";
    }

}
