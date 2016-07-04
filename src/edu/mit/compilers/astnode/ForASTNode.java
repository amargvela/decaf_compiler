package edu.mit.compilers.astnode;

import antlr.collections.AST;
import edu.mit.compilers.AssemblerVisitor;
import edu.mit.compilers.DataflowVisitor;
import edu.mit.compilers.SemanticCheckVisitor;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.instruction.Instruction;
import edu.mit.compilers.utils.Assertion;
import edu.mit.compilers.utils.Enums;

import java.util.LinkedList;

public class ForASTNode extends ASTNode {
    private LocationASTNode location;
    private ExpressionASTNode initialValue;
    private ExpressionASTNode finalValue;
    private IntLiteralASTNode incrementStep;
    private BlockASTNode block;
    private boolean allPathsReturn = false;

    public ForASTNode(AST ast) {
        super(ast);
        Assertion.check(ast.getType() == DecafParserTokenTypes.TK_for);

        assert (ast.getNumberOfChildren() == 3 || ast.getNumberOfChildren() == 4);

        AST child = ast.getFirstChild();
        location = new LocationASTNode(child.getText(), Enums.Type.INT_SINGLE);

        child = child.getNextSibling();
        assert ast.getType() == DecafParserTokenTypes.EXPR;
        initialValue = new ExpressionASTNode(child);

        child = child.getNextSibling();
        assert ast.getType() == DecafParserTokenTypes.EXPR;
        finalValue = new ExpressionASTNode(child);
        if (ast.getNumberOfChildren() == 5) {
            child = child.getNextSibling();
            incrementStep = new IntLiteralASTNode(child);
        }

        child = child.getNextSibling();
        block = new BlockASTNode(child, false);
        
        allPathsReturn = block.allPathsReturn();
    }

    public LocationASTNode getLocation() {
        return location;
    }

    public ExpressionASTNode getInitialValue() {
        return initialValue;
    }
    
    public ExpressionASTNode getFinalValue() {
        return finalValue;
    }
    
    public IntLiteralASTNode getIncrementStep() {
        return incrementStep;
    }
    
    public BlockASTNode getBlock() {
        return block;
    }
    
    public boolean allPathsReturn() {
        return allPathsReturn;
    }
    
    @Override
    public String toString() {
        String answer = "<" + this.getClass().getSimpleName() + ">";
        answer += location.toString();
        answer += initialValue.toString();
        answer += finalValue.toString();
        if (incrementStep != null) {
            answer += incrementStep.toString();
        }
        answer += block.toString();
        answer += "</" + this.getClass().getSimpleName() + ">";
        return answer;
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
