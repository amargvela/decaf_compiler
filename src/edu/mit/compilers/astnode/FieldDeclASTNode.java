package edu.mit.compilers.astnode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import antlr.collections.AST;
import edu.mit.compilers.AssemblerVisitor;
import edu.mit.compilers.DataflowVisitor;
import edu.mit.compilers.SemanticCheckVisitor;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.instruction.Instruction;
import edu.mit.compilers.utils.Assertion;
import edu.mit.compilers.utils.Enums;

public class FieldDeclASTNode extends ASTNode {
    private Enums.Type fieldType;
    private List<SingleFieldDeclASTNode> singleFields = new ArrayList<SingleFieldDeclASTNode>();;

    public FieldDeclASTNode(AST ast) {
        super(ast);
        Assertion.check(ast.getType() == DecafParserTokenTypes.FIELD_DECL);

        AST child = ast.getFirstChild();
        for (int i = 0; i < ast.getNumberOfChildren(); i++) {
            if (child.getType() == DecafParserTokenTypes.TK_int || child.getType() == DecafParserTokenTypes.TK_boolean)
                fieldType = Enums.convertTypeToEnum(child.getType());
            if (child.getType() == DecafParserTokenTypes.ARRAY_DECL) {
                singleFields.add(new SingleFieldDeclASTNode(child, Enums.convertTypeToArray(fieldType)));
            }
            if (child.getType() == DecafParserTokenTypes.ID)
                singleFields.add(new SingleFieldDeclASTNode(child, fieldType));
            child = child.getNextSibling();
        }
    }
    
    public FieldDeclASTNode(SingleFieldDeclASTNode singleFieldDecl) {
        super(null);
        
        this.fieldType = singleFieldDecl.getType();
        singleFields.add(singleFieldDecl);
    }
    
    public List<SingleFieldDeclASTNode> getSingleFieldDecls() {
        return new ArrayList<SingleFieldDeclASTNode>(singleFields);
    }

    @Override
    public String toString() {
        String answer = "";
        answer += "<" + getClass().getSimpleName() + ">";
        for (int i = 0; i < singleFields.size(); i++) {
            answer += singleFields.get(i).toString();
        }
        answer += "</" + getClass().getSimpleName() + ">";
        return answer;
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
}
