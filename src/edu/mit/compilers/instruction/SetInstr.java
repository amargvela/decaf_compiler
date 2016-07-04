package edu.mit.compilers.instruction;

import edu.mit.compilers.utils.Enums;

public class SetInstr extends Instruction {

    private String byteAddress; 
    private Enums.RelationalOp op;
    
    public SetInstr(String label, Enums.RelationalOp op, String byteAddress) {
        super(label);
        this.byteAddress = byteAddress;
        this.op = op;
    }
    
    public SetInstr(Enums.RelationalOp op, String byteAddress) {
        this(null, op, byteAddress);
    }
    
    @Override
    public String getInstruction() {
        switch (op) {
        case EQ:
            return "sete\t" + byteAddress;
        case NEQ:
            return "setne\t" + byteAddress;
        case LT:
            return "setl\t" + byteAddress;
        case GT:
            return "setg\t" + byteAddress;
        case LEQ:
            return "setle\t" + byteAddress;
        case GEQ:
            return "setge\t" + byteAddress;
        default:
            throw new RuntimeException("Not implemented for " + Enums.getOpName(op));
        }
    }
}
