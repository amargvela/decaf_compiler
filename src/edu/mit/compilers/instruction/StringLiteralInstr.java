package edu.mit.compilers.instruction;

/**
 * .string "Example String" - Used to declare string constants in the program
 */
public class StringLiteralInstr extends Instruction {

    private String value;

    // String value is expected to have quotes of its own
    public StringLiteralInstr(String label, String value) {
        super(label);
        this.value = value;
    }

    @Override
    public String getInstruction() {
        return ".string " + value;
    }
}
