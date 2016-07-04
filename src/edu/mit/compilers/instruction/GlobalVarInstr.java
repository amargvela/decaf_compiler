package edu.mit.compilers.instruction;

import edu.mit.compilers.utils.Consts;
import edu.mit.compilers.utils.Enums.Type;

public class GlobalVarInstr extends Instruction {

    private Type type;
    private int length;
    
    public GlobalVarInstr(String name, Type type, int length) {
        super(name); // label is the name
        
        this.type = type;
        this.length = length;
    }

    @Override
    public String getInstruction() {
//      Declaring global fields: (.bss should be declared earlier)
//  NAME:
//      .align  8
//      .size   NAME, 8*SIZE
//      .zero   8*SIZE

        int bytes =  Consts.getUnitSize(type) * length;
        
        StringBuilder builder = new StringBuilder();
        builder.append(".align\t" + Consts.allignSize(type) + "\n");
        builder.append("\t.size\t" + label + ", " + bytes + "\n");
        builder.append("\t.zero\t" + bytes);
        return builder.toString();
    }
}
