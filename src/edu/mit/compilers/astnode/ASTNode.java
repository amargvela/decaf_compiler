package edu.mit.compilers.astnode;

import java.util.LinkedList;
import java.util.logging.Logger;

import antlr.collections.AST;
import edu.mit.compilers.AssemblerVisitor;
import edu.mit.compilers.DataflowVisitor;
import edu.mit.compilers.SemanticCheckVisitor;
import edu.mit.compilers.instruction.Instruction;

public abstract class ASTNode {
    protected static final Logger LOGGER = Logger.getLogger(ASTNode.class.getName());

    protected final AST ast;

    public ASTNode(AST ast) {
        this.ast = ast;
//        LOGGER.log(Level.INFO, "Entered: " + getClass().getSimpleName() + "; AST root: " + ast.getText());
//        LOGGER.log(Level.INFO, "    " + ast.toStringTree());
    }

    public abstract boolean accept(SemanticCheckVisitor visitor);

    public abstract LinkedList<Instruction> accept(AssemblerVisitor visitor);
    
    public abstract void accept(DataflowVisitor visitor);

    public int getLine() {
        return ast != null ? ast.getLine() : -1;
    }
    
    public int getColumn() {
        return ast != null ? ast.getColumn() : -1;
    }
}
