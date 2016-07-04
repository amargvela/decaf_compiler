package edu.mit.compilers.astnode;

import antlr.collections.AST;
import edu.mit.compilers.AssemblerVisitor;
import edu.mit.compilers.DataflowVisitor;
import edu.mit.compilers.SemanticCheckVisitor;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.instruction.Instruction;

import java.util.LinkedList;

public class ContinueASTNode extends ASTNode {

    public ContinueASTNode(AST ast) {
        super(ast);
        assert ast.getType() == DecafParserTokenTypes.TK_continue;
    }

    @Override
    public boolean accept(SemanticCheckVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public LinkedList<Instruction> accept(AssemblerVisitor visitor) {
        return visitor.visit(this);
    }
    
    @Override
    public void accept(DataflowVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "<continue/>";
    }
}
