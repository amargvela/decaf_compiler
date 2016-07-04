package edu.mit.compilers.astnode;

import antlr.collections.AST;
import edu.mit.compilers.AssemblerVisitor;
import edu.mit.compilers.SemanticCheckVisitor;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.instruction.Instruction;
import edu.mit.compilers.utils.Assertion;
import edu.mit.compilers.utils.Enums;
import edu.mit.compilers.utils.Enums.LogicalOp;

import java.util.*;

public class LogicalOpASTNode extends ExpressionASTNode implements BinaryOpASTNode {

    Enums.LogicalOp op;
    ExpressionASTNode leftExpr, rightExpr;

    public LogicalOpASTNode(AST ast) {
        super(ast, true);
        Assertion.check(
                ast.getType() == DecafParserTokenTypes.LOG_OR ||
                ast.getType() == DecafParserTokenTypes.LOG_AND
        );

        op = Enums.convertLogOpToEnum(ast.getText());

        AST node = ast.getFirstChild();
        leftExpr = new ExpressionASTNode(node);
        node = node.getNextSibling();
        rightExpr = new ExpressionASTNode(node);
    }

    public ExpressionASTNode leftOperand() {
        return leftExpr;
    }

    public ExpressionASTNode rightOperand() {
        return rightExpr;
    }
    
    public LogicalOp getOp() {
        return op;
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
        builder.append("<" + getClass().getSimpleName() + " op=\"" + op + "\">");
        builder.append(leftExpr.toString());
        builder.append(rightExpr.toString());
        builder.append("</" + getClass().getSimpleName() + ">");
        return builder.toString();
    }
    
    @Override
    public HashSet<String> generateUsedVariables() {
        HashSet<String> result = new HashSet<String>();
        result.addAll(leftExpr.generateUsedVariables());
        result.addAll(rightExpr.generateUsedVariables());
        return result;
    }
    
    @Override
    public String expressionString(Enums.ExprToStr t) {
        String leftS = leftExpr.expressionString(t);
        String rightS = rightExpr.expressionString(t);
        
        if (rightS.compareTo(leftS) < 0) {
            return "(" + rightS + Enums.getOpName(op) + leftS + ")";
        } else {
            return "(" + leftS + Enums.getOpName(op) + rightS + ")";
        }
    }
    
    @Override
    public boolean containsMethodCall() {
        return leftExpr.containsMethodCall() || rightExpr.containsMethodCall();
    }
    
    @Override
    public List<ExpressionASTNode> allSubexpressions() {
        List<ExpressionASTNode> result = leftExpr.allSubexpressions();
        result.addAll(rightExpr.allSubexpressions());
        if (!containsMethodCall()) {
            result.add(this);
        }
        return result;
    }

    @Override
    public void optimizeSubexpressions(Map<String, LocationASTNode> exprToTemp) {
        LocationASTNode loc = exprToTemp.get(leftExpr.expressionString(Enums.ExprToStr.SYMBOLIC_VALUE));
        if (loc != null) {
            leftExpr = loc;
        } else {
            leftExpr.optimizeSubexpressions(exprToTemp);
        }
        loc = exprToTemp.get(rightExpr.expressionString(Enums.ExprToStr.SYMBOLIC_VALUE));
        if (loc != null) {
            rightExpr = loc;
        } else {
            rightExpr.optimizeSubexpressions(exprToTemp);
        }
    }
}
