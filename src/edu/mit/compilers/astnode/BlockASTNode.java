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

import javax.lang.model.element.VariableElement;

public class BlockASTNode extends ASTNode {

    private List<FieldDeclASTNode> fields = new LinkedList<FieldDeclASTNode>();
    private List<ASTNode> statements = new LinkedList<ASTNode>();
    private boolean allPathsReturn = false;
    private boolean level1Block;
    private static int blockCounter = 0;
    private String name;

    public BlockASTNode(AST ast, boolean isLevel1Block) {
        super(ast);
        this.level1Block = isLevel1Block;
        Assertion.check(ast.getType() == DecafParserTokenTypes.BLOCK);
        name = "b" + blockCounter++;
        for (AST child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
            switch (child.getType()) {
            case DecafParserTokenTypes.FIELD_DECL:
                fields.add(new FieldDeclASTNode(child));
                break;

            case DecafParserTokenTypes.ASSIGN_OP:
            case DecafParserTokenTypes.MODIFY_OP:
                statements.add(new AssignStmtASTNode(child));
                break;

            case DecafParserTokenTypes.METHOD_CALL:
                statements.add(new MethodCallASTNode(child));
                break;

            case DecafParserTokenTypes.TK_if:
                IfASTNode ifASTNode = new IfASTNode(child);
                statements.add(ifASTNode);
                allPathsReturn = allPathsReturn || ifASTNode.allPathsReturn();
                break;

            case DecafParserTokenTypes.TK_for:
                ForASTNode forASTNode = new ForASTNode(child);
                statements.add(forASTNode);
                allPathsReturn = allPathsReturn || forASTNode.allPathsReturn();
                break;

            case DecafParserTokenTypes.TK_while:
                WhileASTNode whileASTNode = new WhileASTNode(child);
                statements.add(whileASTNode);
                allPathsReturn = allPathsReturn || whileASTNode.allPathsReturn();
                break;

            case DecafParserTokenTypes.TK_return:
                statements.add(new ReturnASTNode(child));
                allPathsReturn = true;
                break;

            case DecafParserTokenTypes.TK_continue:
                statements.add(new ContinueASTNode(child));
                break;

            case DecafParserTokenTypes.TK_break:
                statements.add(new BreakASTNode(child));
                break;

            default:
                throw new RuntimeException(
                        "Statement type = " + child.getType() + ", text = " + child.getText() + " not found");
            }

        }
    }

    public String getName() {
        return name;
    }

    public List<FieldDeclASTNode> getFields() {
        return new ArrayList<FieldDeclASTNode>(fields);
    }

    public List<ASTNode> getStatements() {
        return new ArrayList<ASTNode>(statements);
    }
    
    public boolean allPathsReturn() {
        return allPathsReturn;
    }

    public boolean isLevel1Block() {
        return level1Block;
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
        String xmlString = "<" + getClass().getSimpleName() + ">";
        for (FieldDeclASTNode field : fields) {
            xmlString += field.toString();
        }
        for (ASTNode statement : statements) {
            xmlString += statement.toString();
        }
        xmlString += "</" + getClass().getSimpleName() + ">";
        return xmlString;
    }

    @Override
    public void accept(DataflowVisitor visitor) {
        visitor.visit(this);
    }
    
    public void declareField(SingleFieldDeclASTNode singleFieldDecl) {
        fields.add(new FieldDeclASTNode(singleFieldDecl));
    }

    public boolean insertStmtBefore(AssignStmtASTNode next, ASTNode statement) {
        if (next == null) {
            statements.add(statement);
            return true;
        } else if (statements.contains(next)) {
            statements.add(statements.indexOf(next), statement);
            return true;
        }
        return false;
    }

    public void insertStmtBefore(ExpressionASTNode next, ASTNode statement) {
        if (next == null) {
            statements.add(statement);
        }
        for (ASTNode node : statements) {
            boolean match = false;
            if (node instanceof IfASTNode) {
                IfASTNode ifASTNode = (IfASTNode)node;
                if (ifASTNode.getCondition() == next) {
                    match = true;
                }
            } else if (node instanceof ForASTNode) {
                ForASTNode forASTNode = (ForASTNode)node;
                if (forASTNode.getInitialValue() == next || forASTNode.getFinalValue() == next) {
                    match = true;
                }
            } else if (node instanceof WhileASTNode) {
                WhileASTNode whileASTNode = (WhileASTNode)node;
                if (whileASTNode.getCondition() == next) {
                    match = true;
                }
            } else if (node instanceof ReturnASTNode) {
                ReturnASTNode returnASTNode = (ReturnASTNode)node;
                if (returnASTNode.getExpression() == next) {
                    match = true;
                }
            }
            if (match) {
                statements.add(statements.indexOf(node), statement);
                return;
            }
        }
        statements.add(statement);
    }
}
