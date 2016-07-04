package edu.mit.compilers.astnode;

import antlr.collections.AST;
import edu.mit.compilers.AssemblerVisitor;
import edu.mit.compilers.DataflowVisitor;
import edu.mit.compilers.SemanticCheckVisitor;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.instruction.Instruction;
import edu.mit.compilers.utils.Assertion;

import java.util.LinkedList;

public class CalloutDeclASTNode extends ASTNode {

    private String calloutName;

    public CalloutDeclASTNode(AST ast) {
        super(ast);
        Assertion.check(ast.getType() == DecafParserTokenTypes.CALLOUT_DECL);

        calloutName = ast.getFirstChild().getText();
    }
    
    public String getName() {
        return calloutName;
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
        return "<" + this.getClass().getSimpleName() + ">" + calloutName + "</" + this.getClass().getSimpleName() + ">";
    }
}
