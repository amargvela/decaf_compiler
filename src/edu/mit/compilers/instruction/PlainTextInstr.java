package edu.mit.compilers.instruction;

public class PlainTextInstr extends Instruction {

    private String text;

    public PlainTextInstr(String text) {
        super(null);
        this.text = text;
    }

    @Override
    public String getInstruction() {
        return text;
    }
}
