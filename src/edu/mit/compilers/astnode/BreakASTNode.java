package edu.mit.compilers.astnode;

import antlr.collections.AST;
import edu.mit.compilers.AssemblerVisitor;
import edu.mit.compilers.DataflowVisitor;
import edu.mit.compilers.SemanticCheckVisitor;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.instruction.Instruction;
import edu.mit.compilers.utils.Assertion;

import java.util.LinkedList;

public class BreakASTNode extends ASTNode {

    public BreakASTNode(AST ast) {
        super(ast);
        Assertion.check(ast.getType() == DecafParserTokenTypes.TK_break);
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
    public String toString() {
        return "<break/>";
    }

    @Override
    public void accept(DataflowVisitor visitor) {
        visitor.visit(this);
    }
}
