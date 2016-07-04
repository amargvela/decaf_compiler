package edu.mit.compilers.astnode;

import antlr.collections.AST;
import edu.mit.compilers.AssemblerVisitor;
import edu.mit.compilers.LocalDescriptor;
import edu.mit.compilers.MethodCallParam;
import edu.mit.compilers.SemanticCheckVisitor;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.instruction.Instruction;
import edu.mit.compilers.utils.Assertion;
import edu.mit.compilers.utils.Enums;
import edu.mit.compilers.utils.Enums.Type;

import java.util.*;

public class MethodCallASTNode extends ExpressionASTNode {

    private String name;
    private List<MethodCallParam> params = new ArrayList<MethodCallParam>();

    public MethodCallASTNode(AST ast) {
        super(ast, true);
        Assertion.check(ast.getType() == DecafParserTokenTypes.METHOD_CALL);
        AST node = ast.getFirstChild();
        name = node.getText();
        node = node.getNextSibling();
        for (int i = 1; i < ast.getNumberOfChildren(); i++) {
            switch (node.getType()) {
            case DecafParserTokenTypes.STR_LITERAL:
                params.add(new MethodCallParam(node.getText()));
                break;
            case DecafParserTokenTypes.EXPR:
                params.add(new MethodCallParam(new ExpressionASTNode(node)));
                break;
            default:
                throw new IllegalStateException("Such Token was not expected");
            }
            node = node.getNextSibling();
        }
    }

    public String getMethodName() {
        return name;
    }

    public List<MethodCallParam> getParams() {
        return new ArrayList<MethodCallParam>(params);
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
        builder.append("<" + getClass().getSimpleName() + " name=\"" + name + "\">");
        for (int i = 0; i < params.size(); i++) {
            builder.append("<_Parameter type=\"" + params.get(i).getType() + "\">");
            switch (params.get(i).getType()) {
            case STR:
                builder.append(params.get(i).getLiteral());
                break;
            default:
                builder.append(params.get(i).getExpr().toString());
                break;
            }
            builder.append("</_Parameter>");
        }
        builder.append("</" + getClass().getSimpleName() + ">");
        return builder.toString();
    }
    
    @Override
    public String expressionString(Enums.ExprToStr t) {
        if (t == Enums.ExprToStr.VARIABLE_ID) {
            String str = "__method__" + name + "(";
            for (MethodCallParam param : params) {
                switch (param.getType()) {
                    case STR:
                        str += param.getLiteral();
                        break;
                    default:
                        str += param.getExpr().expressionString(t);
                        break;
                }
                str += ",";
            }
            return str + ")";
        } else {
            // always create a new symbolic value, so that its return value never gets cached
            return new LocalDescriptor(name, Type.INT_SINGLE).getSymbolicValue();
        }
    }
    
    @Override
    public boolean containsMethodCall() {
        return true;
    }
    
    @Override
    public List<ExpressionASTNode> allSubexpressions() {
        List<ExpressionASTNode> result = new ArrayList<ExpressionASTNode>();
        for (MethodCallParam param : params) {
            if (param.getType() == Type.EXPR) {
                result.addAll(param.getExpr().allSubexpressions());
            }
        }
        return result;
    }

    @Override
    public void optimizeSubexpressions(Map<String, LocationASTNode> exprToTemp) {
        for (MethodCallParam param : params) {
            if (param.getType() == Type.STR) {
                continue;
            }
            ExpressionASTNode expression = param.getExpr();
            LocationASTNode loc = exprToTemp.get(expression.expressionString(Enums.ExprToStr.SYMBOLIC_VALUE));
            if (loc != null) {
                param.setExpr(loc);
            } else {
                expression.optimizeSubexpressions(exprToTemp);
            }
        }
    }
    
    @Override
    public HashSet<String> generateUsedVariables() {
        HashSet<String> result = new HashSet<String>();
        for (MethodCallParam param : params) {
            if (param.getType() == Type.STR) {
                continue;
            }
            result.addAll(param.getExpr().generateUsedVariables());
        }
        return result;
    }
}
