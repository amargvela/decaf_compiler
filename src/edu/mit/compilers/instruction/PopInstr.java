package edu.mit.compilers.instruction;

/**
 * popq dest - Put current %rsp value into dest and increase %rsp
 */
public class PopInstr extends Instruction {

    private String where;
    
    public PopInstr(String label, String where) {
        super(label);
        this.where = where;
    }
    
    public PopInstr(String where) {
        this(null, where);
    }

    @Override
    public String getInstruction() {
        return "popq\t" + where;
    }
}
