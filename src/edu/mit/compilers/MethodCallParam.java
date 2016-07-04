package edu.mit.compilers;

import edu.mit.compilers.astnode.ExpressionASTNode;
import edu.mit.compilers.utils.Enums;

public class MethodCallParam {

    private Enums.Type type;
    private String literal;
    private ExpressionASTNode expr;

    public MethodCallParam(String literal) {
        type = Enums.Type.STR;
        this.literal = literal;
    }

    public MethodCallParam(ExpressionASTNode expr) {
        type = Enums.Type.EXPR;
        this.expr = expr;
    }

    public Enums.Type getType() {
        return type;
    }

    public void setType(Enums.Type type) {
        this.type = type;
    }

    public String getLiteral() {
        if (literal != null) {
            return literal;
        } else {
            throw new IllegalStateException("Literal is null");
        }
    }

    public ExpressionASTNode getExpr() {
        if (expr != null) {
            return expr;
        } else {
            throw new IllegalStateException("Expression is null");
        }
    }

    public void setExpr(ExpressionASTNode expr) {
        this.expr = expr;
    }
}
