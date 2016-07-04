package edu.mit.compilers.astnode;

import antlr.collections.AST;
import edu.mit.compilers.AssemblerVisitor;
import edu.mit.compilers.DataflowVisitor;
import edu.mit.compilers.SemanticCheckVisitor;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.instruction.Instruction;
import edu.mit.compilers.utils.Assertion;
import edu.mit.compilers.utils.Enums;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MethodDeclASTNode extends ASTNode {

    private Enums.Type methodType;
    private String methodName;
    private List<Enums.Type> parameterTypes = new ArrayList<Enums.Type>();
    private List<String> parameterNames = new ArrayList<String>();
    private BlockASTNode block;

    public MethodDeclASTNode(AST ast) {
        super(ast);
        Assertion.check(ast.getType() == DecafParserTokenTypes.METHOD_DECL);
        AST node = ast.getFirstChild();
        for (int i = 0; i < ast.getNumberOfChildren(); i++) {
            switch (node.getType()) {
            case DecafParserTokenTypes.TK_int:
            case DecafParserTokenTypes.TK_boolean:
            case DecafParserTokenTypes.TK_void:
                if (i == 0) {
                    methodType = Enums.convertTypeToEnum(node.getType());
                    node = node.getNextSibling();
                    i++;
                    methodName = node.getText();
                } else {
                    parameterTypes.add(Enums.convertTypeToEnum(node.getType()));
                    node = node.getNextSibling();
                    i++;
                    parameterNames.add(node.getText());
                }
                break;
            case DecafParserTokenTypes.BLOCK:
                block = new BlockASTNode(node, true);
                break;
            default:
                throw new IllegalStateException("The token "+node.getText() + "(" + node.getType()+") was not expected");
            }
            node = node.getNextSibling();
        }
    }

    public Enums.Type getMethodType() {
        return methodType;
    }

    public String getMethodName() {
        return methodName;
    }

    public ArrayList<Enums.Type> getParameterTypes() {
        return new ArrayList<Enums.Type>(parameterTypes);
    }

    public List<String> getParameterNames() {
        return new ArrayList<String>(parameterNames);
    }

    public BlockASTNode getBlock() {
        return block;
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
        builder.append("<" + getClass().getSimpleName());
        builder.append(" type=\"" + methodType + "\"");
        builder.append(" name=\"" + methodName + "\">");
        for (int i = 0; i < parameterNames.size(); i++) {
            builder.append("<_Parameter");
            builder.append(" type=\"" + parameterTypes.get(i) + "\"");
            builder.append(" name=\"" + parameterNames.get(i) + "\"");
            builder.append(" />");
        }
        builder.append(block.toString());
        builder.append(" </" + getClass().getSimpleName() + ">");
        return builder.toString();
    }
}
