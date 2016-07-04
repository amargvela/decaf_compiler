package edu.mit.compilers.astnode;

import antlr.collections.AST;
import edu.mit.compilers.AssemblerVisitor;
import edu.mit.compilers.SemanticCheckVisitor;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.instruction.Instruction;
import edu.mit.compilers.utils.Assertion;
import edu.mit.compilers.utils.Enums;
import edu.mit.compilers.utils.Enums.RelationalOp;

import java.util.*;

public class RelationalOpASTNode extends ExpressionASTNode implements BinaryOpASTNode {

    RelationalOp op;
    
    private ExpressionASTNode left;
    private ExpressionASTNode right;
    
    public RelationalOpASTNode(AST ast) {
        super(ast, true);
        Assertion.check(ast.getType() == DecafParserTokenTypes.REL_OP || ast.getType() == DecafParserTokenTypes.EQ_OP);
        Assertion.check(ast.getNumberOfChildren() == 2);
        
        op = Enums.convertRelOpToEnum(ast.getText());
        
        AST child = ast.getFirstChild();
        left = new ExpressionASTNode(child);
        right = new ExpressionASTNode(child.getNextSibling());
    }

    public RelationalOpASTNode(RelationalOp op, ExpressionASTNode left, ExpressionASTNode right, Enums.Type type) {
        super(null, true);
        Assertion.check(type != Enums.Type.EXPR);
        this.op = op;
        this.left = left;
        this.right = right;
        this.type = type;
    }

    public Enums.RelationalOp getOp() {
        return op;
    }
    
    public ExpressionASTNode leftOperand() {
        return left;
    }
    
    public ExpressionASTNode rightOperand() {
        return right;
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
        builder.append("<" + getClass().getSimpleName() + " ");
        builder.append("op=\"" + Enums.getOpName(op) + "\">");
        builder.append(left.toString());
        builder.append(right.toString());
        builder.append("</" + getClass().getSimpleName() + ">");
        return builder.toString();
    }
    
    @Override
    public HashSet<String> generateUsedVariables() {
        HashSet<String> result = new HashSet<String>();
        result.addAll(left.generateUsedVariables());
        result.addAll(right.generateUsedVariables());
        return result;
    }
    
    @Override
    public String expressionString(Enums.ExprToStr t) {
        String leftS = left.expressionString(t);
        String rightS = right.expressionString(t);
        String ans;
        
        switch (op) {
        case EQ:
        case NEQ:
            if (leftS.compareTo(rightS) > 0) {
                ans = rightS + Enums.getOpName(op) + leftS;
            } else {
                ans = leftS + Enums.getOpName(op) + rightS;
            }
            break;
        case LEQ:
        case LT:
            ans = leftS + Enums.getOpName(op) + rightS;
            break;
        case GEQ:
            ans = rightS + Enums.getOpName(Enums.RelationalOp.LEQ) + leftS;
            break;
        case GT:
            ans = rightS + Enums.getOpName(Enums.RelationalOp.LT) + leftS;
            break;
        default:
            throw new RuntimeException();
        }
        return "(" + ans + ")";
    }
    
    @Override
    public boolean containsMethodCall() {
        return left.containsMethodCall() || right.containsMethodCall();
    }
    
    @Override
    public List<ExpressionASTNode> allSubexpressions() {
        List<ExpressionASTNode> result = left.allSubexpressions();
        result.addAll(right.allSubexpressions());
        if (!containsMethodCall()) {
            result.add(this);
        }
        return result;
    }

    @Override
    public void optimizeSubexpressions(Map<String, LocationASTNode> exprToTemp) {
        LocationASTNode loc = exprToTemp.get(left.expressionString(Enums.ExprToStr.SYMBOLIC_VALUE));
        if (loc != null) {
            left = loc;
        } else {
            left.optimizeSubexpressions(exprToTemp);
        }

        loc = exprToTemp.get(right.expressionString(Enums.ExprToStr.SYMBOLIC_VALUE));
        if (loc != null) {
            right = loc;
        } else {
            right.optimizeSubexpressions(exprToTemp);
        }
    }
}
