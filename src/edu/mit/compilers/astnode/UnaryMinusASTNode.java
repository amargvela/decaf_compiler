package edu.mit.compilers.astnode;

import antlr.collections.AST;
import edu.mit.compilers.AssemblerVisitor;
import edu.mit.compilers.SemanticCheckVisitor;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.instruction.Instruction;
import edu.mit.compilers.utils.Assertion;
import edu.mit.compilers.utils.Enums;

import java.util.*;

public class UnaryMinusASTNode extends ExpressionASTNode {

    private ExpressionASTNode expression;
    
    public UnaryMinusASTNode(AST ast) {
        super(ast, true);
        Assertion.check(ast.getType() == DecafParserTokenTypes.MINUS);
        Assertion.check(ast.getNumberOfChildren() == 1);
        
        expression = new ExpressionASTNode(ast.getFirstChild());
    }

    public ExpressionASTNode getExpression() {
        return expression;
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
        StringBuilder builder = new StringBuilder();
        builder.append("<" + getClass().getSimpleName() + ">");
        builder.append(expression.toString());
        builder.append("</" + getClass().getSimpleName() + ">");
        return builder.toString();
    }
    
    @Override
    public HashSet<String> generateUsedVariables() {
        return expression.generateUsedVariables();
    }
    
    @Override
    public String expressionString(Enums.ExprToStr t) {
        return "-" + expression.expressionString(t);
    }
    
    @Override
    public boolean containsMethodCall() {
        return expression.containsMethodCall();
    }
    
    @Override
    public List<ExpressionASTNode> allSubexpressions() {
        List<ExpressionASTNode> result = expression.allSubexpressions();
        if (!containsMethodCall()) {
            result.add(this);
        }
        return result;
    }

    @Override
    public void optimizeSubexpressions(Map<String, LocationASTNode> exprToTemp) {
        LocationASTNode loc = exprToTemp.get(expression.expressionString(Enums.ExprToStr.SYMBOLIC_VALUE));
        if (loc != null) {
            expression = loc;
        } else {
            expression.optimizeSubexpressions(exprToTemp);
        }
    }
}
