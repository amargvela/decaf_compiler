package edu.mit.compilers.astnode;

import antlr.collections.AST;
import edu.mit.compilers.AssemblerVisitor;
import edu.mit.compilers.DataflowVisitor;
import edu.mit.compilers.SemanticCheckVisitor;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.instruction.Instruction;
import edu.mit.compilers.utils.Assertion;

import java.util.LinkedList;

public class IfASTNode extends ASTNode {

    private ExpressionASTNode condition;
    private BlockASTNode blockTrue;
    private BlockASTNode blockFalse;
    private boolean allPathsReturn = false;

    public IfASTNode(AST ast) {
        super(ast);
        Assertion.check(ast.getType() == DecafParserTokenTypes.TK_if);
        Assertion.check(ast.getNumberOfChildren() == 2 || ast.getNumberOfChildren() == 3);

        AST node = ast.getFirstChild();
        condition = new ExpressionASTNode(node);
        node = node.getNextSibling();
        blockTrue = new BlockASTNode(node, false);
        if (ast.getNumberOfChildren() == 3) {
            blockFalse = new BlockASTNode(node.getNextSibling(), false);
            allPathsReturn = blockTrue.allPathsReturn() && blockFalse.allPathsReturn();
        } else {
            blockFalse = null;
            allPathsReturn = false;
        }
    }
    
    public ExpressionASTNode getCondition() {
        return condition;
    }
    
    public BlockASTNode getBlockTrue() {
        return blockTrue;
    }
    
    public BlockASTNode getBlockFalse() {
        return blockFalse;
    }
    
    public boolean hasElse() {
        return blockFalse != null;
    }
    
    public boolean allPathsReturn() {
        return allPathsReturn;
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
        StringBuilder builder = new StringBuilder();
        builder.append("<" + getClass().getSimpleName() + ">");
        builder.append(condition.toString());
        builder.append(blockTrue.toString());
        if (hasElse()) {
            builder.append(blockFalse.toString());
        }
        builder.append("</" + getClass().getSimpleName() + ">");
        return builder.toString();
    }
}
