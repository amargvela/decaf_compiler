package edu.mit.compilers.astnode;

import antlr.collections.AST;
import edu.mit.compilers.AssemblerVisitor;
import edu.mit.compilers.DataflowVisitor;
import edu.mit.compilers.SemanticCheckVisitor;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.instruction.Instruction;
import edu.mit.compilers.utils.Assertion;
import edu.mit.compilers.utils.Enums;

import java.util.LinkedList;

public class SingleFieldDeclASTNode extends ASTNode {

    private Enums.Type type;
    private String name;
    private int length;

    public SingleFieldDeclASTNode(AST ast, Enums.Type type) {
        super(ast);
        this.type = type;
        if (Enums.isTypeArray(type)) {
            Assertion.check(ast.getType() == DecafParserTokenTypes.ARRAY_DECL);
            AST child = ast.getFirstChild();
            this.name = child.getText();
            child = child.getNextSibling();
            try {
                this.length = Integer.parseInt(child.getText());
            } catch (NumberFormatException e) {
                this.length = -1;
            }
        } else {
            Assertion.check(ast.getType() == DecafParserTokenTypes.ID);
            this.name = ast.getText();
            this.length = 0;
        }
    }
    
    public SingleFieldDeclASTNode(Enums.Type type, String name) {
        super(null);
        this.type = type;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Enums.Type getType() {
        return type;
    }

    public int getLength() {
        return length;
    }
    
    public boolean isArray() {
        return Enums.isTypeArray(type);
    }

    @Override
    public String toString() {
        return "<" + getClass().getSimpleName() + " name='" + name + "' type='" + Enums.getTypeName(type) + "' length='" + length + "'/>";
    }

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
}
