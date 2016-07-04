package edu.mit.compilers.astnode;

import antlr.collections.AST;
import edu.mit.compilers.AssemblerVisitor;
import edu.mit.compilers.SemanticCheckVisitor;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.instruction.Instruction;
import edu.mit.compilers.utils.Enums;

import java.util.*;

public class IntLiteralASTNode extends ExpressionASTNode {

    private Long value;
    private boolean alreadyEvaluated;

    public IntLiteralASTNode(AST ast) {
        super(ast, true);
        type = Enums.Type.INT_SINGLE;
    }

    public IntLiteralASTNode(long value) {
        super(null, true);
        this.alreadyEvaluated = true;
        this.value = value;
        this.type = Enums.Type.INT_SINGLE;
    }

    @Override
    public boolean accept(SemanticCheckVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public LinkedList<Instruction> accept(AssemblerVisitor visitor) {
        return visitor.visit(this);
    }

    // Static function for any IntLiteral Evaluation (can be used by MINUS class)
    // Returns null if parse error happened
    // Throws exception if wrong type was passed
    public static Long evaluate(String rawValue, int rawType) {
        switch(rawType) {
            case DecafParserTokenTypes.INT_LITERAL:
                try {
                    if (rawValue.startsWith("0x")) {
                        return Long.parseLong(rawValue.substring(2), 16);
                    } else {
                        return Long.parseLong(rawValue);
                    }
                } catch (NumberFormatException e) {
                    return null;
                }
            case DecafParserTokenTypes.CHAR_LITERAL:
                return (long)Character.getNumericValue(rawValue.charAt(1));
            default:
                throw new IllegalStateException("IntLiteral takes CHAR_LITERAL or INT_LITERAL");
        }
    }

    // Evaluate the IntLiteral only once in its lifetime.
    private void evaluateOnce() {
        if (!alreadyEvaluated) {
            value = IntLiteralASTNode.evaluate(ast.getText(), ast.getType());
            alreadyEvaluated = true;
        }
    }

    public long getValue() {
        evaluateOnce();
        if (value != null) {
            return value;
        } else {
            throw new RuntimeException("There was an error while parsing the IntLiteral");
        }
    }

    public boolean isParsedSuccessfully() {
        evaluateOnce();
        return value != null;
    }

    @Override
    public String toString() {
        evaluateOnce();
        StringBuilder builder = new StringBuilder();
        builder.append("<"+getClass().getSimpleName());
        builder.append(" rawValue=\""+ast.getText()+"\"");
        builder.append(" value=\""+value+"\"/>");
        return builder.toString();
    }
    
    @Override
    public String expressionString(Enums.ExprToStr t) {
        return Long.toString(getValue());
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
