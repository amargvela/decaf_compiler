package edu.mit.compilers.instruction;

/**
 * enter $x, $0 - Sets up a procedure’s stack frame by first pushing the current value of %rbp on to the stack,
 * storing the current value of %rsp in %rbp, and finally decreasing %rsp to make room for x byte-sized local variables.
 */
public class EnterInstr extends Instruction {

    private int bytes;

    public EnterInstr(String label, int bytes) {
        super(label);
        this.bytes = bytes;
    }

    @Override
    public String getInstruction() {
        return "enter\t$" + bytes + ", $0";
    }
}
