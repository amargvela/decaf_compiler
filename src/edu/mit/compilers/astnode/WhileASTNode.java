package edu.mit.compilers.astnode;

import antlr.collections.AST;
import edu.mit.compilers.AssemblerVisitor;
import edu.mit.compilers.DataflowVisitor;
import edu.mit.compilers.SemanticCheckVisitor;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.instruction.Instruction;
import edu.mit.compilers.utils.Assertion;

import java.util.LinkedList;

public class WhileASTNode extends ASTNode {
    private ExpressionASTNode condition;
    private BlockASTNode block;
    private boolean allPathsReturn;

    public WhileASTNode(AST ast) {
        super(ast);
        Assertion.check(ast.getType() == DecafParserTokenTypes.TK_while);
        Assertion.check(ast.getNumberOfChildren() == 2);
        
        AST child = ast.getFirstChild();
        condition = new ExpressionASTNode(child);

        child = child.getNextSibling();
        block = new BlockASTNode(child, false);
        
        allPathsReturn = block.allPathsReturn();
    }
    
    public ExpressionASTNode getCondition() {
        return condition;
    }

    public BlockASTNode getBlock() {
        return block;
    }
    
    public boolean allPathsReturn() {
        return allPathsReturn;
    }
    
    @Override
    public String toString() {
        String xml = "<" + this.getClass().getSimpleName() + ">";
        xml += condition.toString();
        xml += block.toString();
        xml += "</" + this.getClass().getSimpleName() + ">";
        return xml;
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
}
