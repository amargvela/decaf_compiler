package edu.mit.compilers.instruction;

import edu.mit.compilers.utils.Enums;

/**
 * jmp target - Jump unconditionally to target, which is specified as a memory location (for example, a label).
 * je target  - Jump to target if the last comparison had the corresponding result (je: equality; jne: inequality).
 * jne target
 * ...
 */
public class JumpInstr extends Instruction {

    private String address;
    private Enums.RelationalOp op;

    public JumpInstr(String label, String address, Enums.RelationalOp op) {
        super(label);
        this.address = address;
        this.op = op;
    }
    
    public JumpInstr(String address, Enums.RelationalOp op) {
        this(null, address, op);
    }
    
    public JumpInstr(String address) {
        this(address, null);
    }

    @Override
    public String getInstruction() {
        String instruction = "jmp";
        if (op != null) {
            switch(op) {
                case EQ:
                    instruction = "je";
                    break;
                case NEQ:
                    instruction = "jne";
                    break;
                case LT:
                    instruction = "jl";
                    break;
                case LEQ:
                    instruction = "jle";
                    break;
                case GT:
                    instruction = "jg";
                    break;
                case GEQ:
                    instruction = "jge";
                    break;
            }
        }
        return instruction + "\t" + address;
    }
}
