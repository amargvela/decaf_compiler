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
import java.util.List;
import java.util.Map;

public class AssignStmtASTNode extends ASTNode {

    private String opString;
    private Enums.Assignment assignment;
    private LocationASTNode location;
    private ExpressionASTNode expression;
    
    public AssignStmtASTNode(AST ast) {
        super(ast);
        Assertion.check((ast.getType() == DecafParserTokenTypes.ASSIGN_OP ||
                        ast.getType() == DecafParserTokenTypes.MODIFY_OP) &&
                        ast.getNumberOfChildren() == 2);

        opString = ast.getText();
        assignment = Enums.convertStrToAssignment(opString);
        AST child = ast.getFirstChild();
        location = new LocationASTNode(child);
        expression = new ExpressionASTNode(child.getNextSibling());
    }

    public AssignStmtASTNode(String opString, LocationASTNode location, ExpressionASTNode expression) {
        super(null);
        if (location == null || expression == null) {
            throw new IllegalArgumentException("AssignStmtASTNode: location and expression must not be null");
        }
        this.opString = opString;
        this.assignment = Enums.convertStrToAssignment(opString);
        this.location = location;
        this.expression = expression;
    }

    public Enums.Assignment getAssignment() {
        return assignment;
    }

    public LocationASTNode getLocation() {
        return location;
    }

    public ExpressionASTNode getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        String xml = "<" + getClass().getSimpleName() + ">";
        switch (assignment) {
        case EQUALS:
            xml += "=";
            break;
        case PLUS_EQ:
            xml += "+=";
            break;
        case MINUS_EQ:
            xml += "-=";
            break;
        }
        xml += location.toString();
        xml += expression.toString();
        xml += "</" + getClass().getSimpleName() + ">";
        return xml;
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
    
    public String assignStmtString(Enums.ExprToStr t) {
        return location.expressionString(t) + opString + expression.expressionString(t);
    }
    
    public List<ExpressionASTNode> allSubexpressions() {
        List<ExpressionASTNode> result = expression.allSubexpressions();
        if (location.isArray()) {
            result.addAll(location.allSubexpressions());
        }
        return result;
    }

    public void optimizeSubexpressions(Map<String, LocationASTNode> exprToTemp) {
        location.getDescriptor().setSymbolicValue();
        LocationASTNode loc = exprToTemp.get(expression.expressionString(Enums.ExprToStr.SYMBOLIC_VALUE));
        if (loc != null) {
            expression = loc;
        } else {
            expression.optimizeSubexpressions(exprToTemp);
        }
    }

    boolean skip = false;
    public void setSkip() {
        skip = true;
    }
    
    public boolean isSkip() {
        return skip;
    }
}
