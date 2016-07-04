package edu.mit.compilers.astnode;

import antlr.collections.AST;
import edu.mit.compilers.AssemblerVisitor;
import edu.mit.compilers.SemanticCheckVisitor;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.instruction.Instruction;
import edu.mit.compilers.utils.Assertion;
import edu.mit.compilers.utils.Enums;

import java.util.*;

public class ArithmeticOpASTNode extends ExpressionASTNode implements BinaryOpASTNode {

    private Enums.ArithmeticOp op;
    private ExpressionASTNode left;
    private ExpressionASTNode right;

    public ArithmeticOpASTNode(AST ast) {
        this(ast, ast.getNumberOfChildren() - 1);
    }

    public ArithmeticOpASTNode(AST ast, int r) {
        super(ast, true);
        Assertion.check(ast.getType() == DecafParserTokenTypes.ARITH);
        Assertion.check(ast.getNumberOfChildren() % 2 == 1 && ast.getNumberOfChildren() > r);
        AST opAST = ast.getFirstChild();
        for (int i=0; i<r-1;i++){
            opAST = opAST.getNextSibling();
        }
        AST rightAST = opAST.getNextSibling();
        Assertion.check(opAST.getType() == DecafParserTokenTypes.PLUS ||
                opAST.getType() == DecafParserTokenTypes.MINUS ||
                opAST.getType() == DecafParserTokenTypes.MUL_OP);

        op = Enums.convertArithOpToEnum(opAST.getText());
        if (r == 2) {
            left = new ExpressionASTNode(ast.getFirstChild());
        } else {
            left = new ArithmeticOpASTNode(ast, r - 2);
        }
        right = new ExpressionASTNode(rightAST);
    }

    public ArithmeticOpASTNode(Enums.ArithmeticOp op, ExpressionASTNode left, ExpressionASTNode right, Enums.Type type) {
        super(null, true);
        Assertion.check(type != Enums.Type.EXPR);
        this.op = op;
        this.left = left;
        this.right = right;
        this.type = type;
    }
    
    public Enums.ArithmeticOp getOp() {
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
        
        if ((op == Enums.ArithmeticOp.PLUS || op == Enums.ArithmeticOp.MUL) && rightS.compareTo(leftS) < 0) {
            return "(" + rightS + Enums.getOpName(op) + leftS + ")";
        } else {
            return "(" + leftS + Enums.getOpName(op) + rightS + ")";
        }
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
