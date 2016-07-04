package edu.mit.compilers.astnode;

import antlr.collections.AST;
import edu.mit.compilers.AssemblerVisitor;
import edu.mit.compilers.DataflowVisitor;
import edu.mit.compilers.SemanticCheckVisitor;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.instruction.Instruction;
import edu.mit.compilers.utils.Assertion;
import edu.mit.compilers.utils.Enums;

import java.util.*;

public class ExpressionASTNode extends ASTNode {

    private ExpressionASTNode expression;
    Enums.Type type = Enums.Type.EXPR;
    
    public ExpressionASTNode(AST ast, boolean skip) {
        super(ast);
    }

    public ExpressionASTNode(AST ast) {
        super(ast);
        Assertion.check(
            ast.getType() == DecafParserTokenTypes.EXPR ||
            ast.getType() == DecafParserTokenTypes.QMARK ||
            ast.getType() == DecafParserTokenTypes.LOG_OR ||
            ast.getType() == DecafParserTokenTypes.LOG_AND ||
            ast.getType() == DecafParserTokenTypes.REL_OP ||
            ast.getType() == DecafParserTokenTypes.EQ_OP ||
            ast.getType() == DecafParserTokenTypes.ARITH ||
            ast.getType() == DecafParserTokenTypes.MINUS ||
            ast.getType() == DecafParserTokenTypes.LOG_NOT ||
            ast.getType() == DecafParserTokenTypes.ARRAY_LEN ||
            ast.getType() == DecafParserTokenTypes.INT_LITERAL ||
            ast.getType() == DecafParserTokenTypes.CHAR_LITERAL ||
            ast.getType() == DecafParserTokenTypes.TK_true ||
            ast.getType() == DecafParserTokenTypes.TK_false ||
            ast.getType() == DecafParserTokenTypes.METHOD_CALL ||
            ast.getType() == DecafParserTokenTypes.LOCATION
        );

        AST node;
        if (ast.getType() == DecafParserTokenTypes.EXPR) {
            node = ast.getFirstChild();
        } else {
            node = ast;
        }

        switch (node.getType()) {
        case DecafParserTokenTypes.QMARK:
            expression = new TernaryOpASTNode(node);
            break;

        case DecafParserTokenTypes.LOG_OR:
        case DecafParserTokenTypes.LOG_AND:
            expression = new LogicalOpASTNode(node);
            break;

        case DecafParserTokenTypes.REL_OP:
        case DecafParserTokenTypes.EQ_OP:
            expression = new RelationalOpASTNode(node);
            break;
            
        case DecafParserTokenTypes.ARITH:
            if (node.getNumberOfChildren() == 1) {
                expression = new ExpressionASTNode(node.getFirstChild());
            } else {
                expression = new ArithmeticOpASTNode(node);
            }
            break;

        case DecafParserTokenTypes.MINUS:
            expression = new UnaryMinusASTNode(node);
            break;

        case DecafParserTokenTypes.LOG_NOT:
            expression = new UnaryNotASTNode(node);
            break;

        case DecafParserTokenTypes.ARRAY_LEN:
            expression = new ArrayLenASTNode(node);
            break;

        case DecafParserTokenTypes.EXPR:
            expression = new ExpressionASTNode(node);
            break;

        case DecafParserTokenTypes.INT_LITERAL:
        case DecafParserTokenTypes.CHAR_LITERAL:
            expression = new IntLiteralASTNode(node);
            break;

        case DecafParserTokenTypes.TK_true:
        case DecafParserTokenTypes.TK_false:
            expression = new BoolLiteralASTNode(node);
            break;

        case DecafParserTokenTypes.METHOD_CALL:
            expression = new MethodCallASTNode(node);
            break;

        case DecafParserTokenTypes.LOCATION:
            expression = new LocationASTNode(node);
            break;

        default:
            throw new RuntimeException("Token type not recognized: type = " + node.getType() + ", " + node.getText());
        }   
    }

    public Enums.Type getType() {
        return type;
    }

    // Used by visitor during semantic check
    public void setType(Enums.Type type) {
        this.type = type;
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
    public void accept(DataflowVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<" + getClass().getSimpleName() + ">");
        builder.append(expression.toString());
        builder.append("</" + getClass().getSimpleName() + ">");
        return builder.toString();
    }
    
    public HashSet<String> generateUsedVariables() {
        if (expression == null) {
            return new HashSet<String>();
        }
        HashSet<String> usedVariables = expression.generateUsedVariables();
        if (usedVariables != null)
            return usedVariables;
        else
            return new HashSet<String>(); 
    }
    
    public String expressionString(Enums.ExprToStr t) {
        return expression.expressionString(t);
    }
    
    public boolean containsMethodCall() {
        return expression.containsMethodCall();
    }
    
    public List<ExpressionASTNode> allSubexpressions() {
        return expression.allSubexpressions();
    }

    public void optimizeSubexpressions(Map<String, LocationASTNode> exprToTemp) {
        LocationASTNode loc = exprToTemp.get(expression.expressionString(Enums.ExprToStr.SYMBOLIC_VALUE));
        if (loc != null) {
            expression = loc;
        } else {
            expression.optimizeSubexpressions(exprToTemp);
        }
    }
}
