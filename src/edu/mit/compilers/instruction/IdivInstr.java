package edu.mit.compilers.instruction;

/**
 * idiv divisor	- Divide rdx:rax by divisor. Store quotient in rax and store remainder in rdx.
 */
public class IdivInstr extends Instruction {

    private String divisor;

    public IdivInstr(String label, String divisor) {
        super(label);
        this.divisor = divisor;
    }

    @Override
    public String getInstruction() {
        return "idiv\t" + divisor;
    }
}
