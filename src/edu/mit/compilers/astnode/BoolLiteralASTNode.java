package edu.mit.compilers.astnode;

import antlr.collections.AST;
import edu.mit.compilers.AssemblerVisitor;
import edu.mit.compilers.SemanticCheckVisitor;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.instruction.Instruction;
import edu.mit.compilers.utils.Enums;

import java.util.*;

public class BoolLiteralASTNode extends ExpressionASTNode {
    private boolean value;
    
    public BoolLiteralASTNode(AST ast) {
        super(ast, true);
        type = Enums.Type.BOOL_SINGLE;
        
        switch (ast.getType()) {
        case DecafParserTokenTypes.TK_true:
            value = true;
            break;
        case DecafParserTokenTypes.TK_false:
            value = false;
            break;
        default: 
            throw new IllegalStateException("Boolean literal must be either 'TK_true' or 'TK_false'");
        }
    }
    
    public boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        String xml = "<" + this.getClass().getSimpleName() + ">";
        xml += value;
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
    public String expressionString(Enums.ExprToStr t) {
        return Boolean.toString(value);
    }
    
    @Override
    public boolean containsMethodCall() {
        return false;
    }
    
    @Override
    public List<ExpressionASTNode> allSubexpressions() {
        return new ArrayList<ExpressionASTNode>();
    }

    @Override
    public void optimizeSubexpressions(Map<String, LocationASTNode> exprToTemp) {

    }
}
