package edu.mit.compilers.astnode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.mit.compilers.AssemblerVisitor;
import edu.mit.compilers.DataflowVisitor;
import edu.mit.compilers.SemanticCheckVisitor;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.instruction.Instruction;
import edu.mit.compilers.utils.Assertion;
import antlr.collections.AST;

public class ProgramASTNode extends ASTNode {

    private List<CalloutDeclASTNode> callouts = new ArrayList<CalloutDeclASTNode>();
    private List<FieldDeclASTNode> fields = new ArrayList<FieldDeclASTNode>();
    private List<MethodDeclASTNode> methods = new ArrayList<MethodDeclASTNode>();

    public ProgramASTNode(AST ast) {
        super(ast);
        Assertion.check(ast.getType() == DecafParserTokenTypes.PROGRAM);

        AST node = ast.getFirstChild();
        for (int i = 0; i < ast.getNumberOfChildren(); i++) {
            if (node.getType() == DecafParserTokenTypes.METHOD_DECL) {
                methods.add(new MethodDeclASTNode(node));
            } else if (node.getType() == DecafParserTokenTypes.FIELD_DECL) {
                fields.add(new FieldDeclASTNode(node));
            } else if (node.getType() == DecafParserTokenTypes.CALLOUT_DECL) {
                callouts.add(new CalloutDeclASTNode(node));
            }
            node = node.getNextSibling();
        }
    }

    @Override
    public boolean accept(SemanticCheckVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public LinkedList<Instruction> accept(AssemblerVisitor visitor) {
        return null;
    }
    
    @Override
    public void accept(DataflowVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        String programXML = "<" + getClass().getSimpleName() + ">";
        for (CalloutDeclASTNode callout : callouts) {
            programXML += callout.toString();
        }
        for (FieldDeclASTNode field : fields) {
            programXML += field.toString();
        }
        for (MethodDeclASTNode method : methods) {
            programXML += method.toString();
        }
        programXML += "</" + getClass().getSimpleName() + ">";
        return programXML;
    }

    public List<CalloutDeclASTNode> getCallouts() {
        return new ArrayList<CalloutDeclASTNode>(callouts);
    }

    public List<FieldDeclASTNode> getFields() {
        return new ArrayList<FieldDeclASTNode>(fields);
    }

    public List<MethodDeclASTNode> getMethods() {
        return new ArrayList<MethodDeclASTNode>(methods);
    }


}
