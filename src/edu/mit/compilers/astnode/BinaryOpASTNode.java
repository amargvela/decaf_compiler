package edu.mit.compilers.astnode;

import edu.mit.compilers.utils.Enums.Type;

public interface BinaryOpASTNode {

    ExpressionASTNode leftOperand();

    ExpressionASTNode rightOperand();

    void setType(Type expr);
    
    int getLine();

    int getColumn();
}
