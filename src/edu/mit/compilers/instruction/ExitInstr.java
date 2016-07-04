package edu.mit.compilers.instruction;

public class ExitInstr extends Instruction {

    public ExitInstr() {
        super(null);
    }

    @Override
    public String getInstruction() {
        return "sysexit";
    }

}
