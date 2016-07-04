package edu.mit.compilers.astnode;

import antlr.collections.AST;
import edu.mit.compilers.AssemblerVisitor;
import edu.mit.compilers.SemanticCheckVisitor;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.instruction.Instruction;
import edu.mit.compilers.utils.Assertion;
import edu.mit.compilers.utils.Enums;

import java.util.*;

public class TernaryOpASTNode extends ExpressionASTNode {

    private ExpressionASTNode boolExpr, trueExpr, falseExpr;

    public TernaryOpASTNode(AST ast) {
        super(ast, true);
        Assertion.check(ast.getType() == DecafParserTokenTypes.QMARK);
        AST node = ast.getFirstChild();
        boolExpr = new ExpressionASTNode(node);
        node = node.getNextSibling();
        trueExpr = new ExpressionASTNode(node);
        node = node.getNextSibling();
        falseExpr = new ExpressionASTNode(node);
    }
    
    public ExpressionASTNode getBoolExpression() {
        return boolExpr;
    }

    public ExpressionASTNode getTrueExpression() {
        return trueExpr;
    }
    
    public ExpressionASTNode getFalseExpression() {
        return falseExpr;
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
        builder.append(boolExpr.toString());
        builder.append(trueExpr.toString());
        builder.append(falseExpr.toString());
        builder.append("</" + getClass().getSimpleName() + ">");
        return builder.toString();
    }
    
    @Override
    public HashSet<String> generateUsedVariables() {
        HashSet<String> result = new HashSet<String>();
        result.addAll(boolExpr.generateUsedVariables());
        result.addAll(trueExpr.generateUsedVariables());
        result.addAll(falseExpr.generateUsedVariables());
        return result;
    }
    
    @Override
    public String expressionString(Enums.ExprToStr t) {
        return "(" + boolExpr.expressionString(t) + ":" + trueExpr.expressionString(t) + "?" + falseExpr.expressionString(t) + ")";
    }
    
    @Override
    public boolean containsMethodCall() {
        return boolExpr.containsMethodCall() || trueExpr.containsMethodCall() || falseExpr.containsMethodCall();
    }
    
    @Override
    public List<ExpressionASTNode> allSubexpressions() {
        List<ExpressionASTNode> result = boolExpr.allSubexpressions();
        result.addAll(trueExpr.allSubexpressions());
        result.addAll(falseExpr.allSubexpressions());
        if (!containsMethodCall()) {
            result.add(this);
        }
        return result;
    }

    @Override
    public void optimizeSubexpressions(Map<String, LocationASTNode> exprToTemp) {
        LocationASTNode loc = exprToTemp.get(boolExpr.expressionString(Enums.ExprToStr.SYMBOLIC_VALUE));
        if (loc != null) {
            boolExpr = loc;
        } else {
            boolExpr.optimizeSubexpressions(exprToTemp);
        }

        loc = exprToTemp.get(trueExpr.expressionString(Enums.ExprToStr.SYMBOLIC_VALUE));
        if (loc != null) {
            trueExpr = loc;
        } else {
            trueExpr.optimizeSubexpressions(exprToTemp);
        }

        loc = exprToTemp.get(falseExpr.expressionString(Enums.ExprToStr.SYMBOLIC_VALUE));
        if (loc != null) {
            falseExpr = loc;
        } else {
            falseExpr.optimizeSubexpressions(exprToTemp);
        }

    }
}
