package edu.mit.compilers.instruction;

import edu.mit.compilers.utils.Enums;

/**
 * mov src, dest - Copies a value from a register, immediate value or memory address to a register or memory address.
 * cmove %src, %dest - Copies from register %src to register %dest if the last comparison operation had the
 *                      corresponding result (
 *                          cmove: equality,
 *                          cmovne: inequality,
 *                          cmovg: greater,
 *                          cmovl: less,
 *                          cmovge: greater or equal,
 *                          cmovle: less or equal
 *                      ).
 */
public class MoveInstr extends Instruction {

    private String a;
    private String b;
    private Enums.RelationalOp op;

    public MoveInstr(String label, String a, String b, Enums.RelationalOp op) {
        super(label);
        this.a = a;
        this.b = b;
        this.op = op;
    }
    
    public MoveInstr(String a, String b, Enums.RelationalOp op) {
        this(null, a, b, op);
    }
    
    public MoveInstr(String label, String a, String b) {
        this(label, a, b, null);
    }
    
    public MoveInstr(String a, String b) {
        this(null, a, b);
    }

    @Override
    public String getInstruction() {
        String instruction = "movq";
        if (op != null) {
            switch(op) {
                case EQ:
                    instruction = "cmove";
                    break;
                case NEQ:
                    instruction = "cmovne";
                    break;
                case LT:
                    instruction = "cmovl";
                    break;
                case LEQ:
                    instruction = "cmovle";
                    break;
                case GT:
                    instruction = "cmovg";
                    break;
                case GEQ:
                    instruction = "cmovge";
                    break;
            }
        }
        return instruction + "\t" + a + ", " + b;
    }
}
