package edu.mit.compilers.astnode;

import antlr.collections.AST;
import edu.mit.compilers.AssemblerVisitor;
import edu.mit.compilers.SemanticCheckVisitor;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.instruction.Instruction;
import edu.mit.compilers.utils.Assertion;
import edu.mit.compilers.utils.Enums;

import java.util.*;

public class ArrayLenASTNode extends ExpressionASTNode {
    
    private String name;
    private int length;
    
    public ArrayLenASTNode(AST ast) {
        super(ast, true);
        Assertion.check(ast.getType() == DecafParserTokenTypes.ARRAY_LEN);
        Assertion.check(ast.getNumberOfChildren() == 1);
        
        name = ast.getFirstChild().getText();
        setType(Enums.Type.INT_SINGLE);
    }

    public String getName() {
        return name;
    }
    
    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public String toString() {
        String xml = "<" + this.getClass().getSimpleName() + ">";
        xml += name;
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
        return Integer.toString(length);
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
