package edu.mit.compilers.astnode;

import antlr.collections.AST;
import edu.mit.compilers.AssemblerVisitor;
import edu.mit.compilers.LocalDescriptor;
import edu.mit.compilers.SemanticCheckVisitor;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.instruction.Instruction;
import edu.mit.compilers.utils.Assertion;
import edu.mit.compilers.utils.Enums;

import java.util.*;

public class LocationASTNode extends ExpressionASTNode {
    
    private Enums.Location locationType;
    private String name;
    private ExpressionASTNode index;
    private LocalDescriptor descriptor;
        
    public LocationASTNode(AST ast) {
        super(ast, true);
        Assertion.check(ast.getType() == DecafParserTokenTypes.LOCATION);
        
        AST child = ast.getFirstChild();

        name = child.getText();
        if (ast.getNumberOfChildren() > 1) {
            locationType = Enums.Location.ARRAY;
            child = child.getNextSibling();
            index = new ExpressionASTNode(child);
        } else {
            locationType = Enums.Location.SINGLE;
        }
    }

    public LocationASTNode(String variable, Enums.Type t) {
        super(null, true);
        locationType = Enums.Location.SINGLE;
        name = variable;
        descriptor = new LocalDescriptor(name, t);
    }

    public Enums.Location getLocationType() {
        return locationType;
    }

    public String getName() {
        return name;
    }

    public LocalDescriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(LocalDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public ExpressionASTNode getIndex() {
        return index;
    }
    
    public boolean isArray() {
        return locationType == Enums.Location.ARRAY;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<" + this.getClass().getSimpleName() + ">");
        builder.append("<_Type>" + locationType.name() +"</_Type>");
        builder.append("<_Name>" +name +"</_Name>");
        if (index != null) {
            builder.append(index.toString());
        }
        builder.append("</" + this.getClass().getSimpleName() + ">");
        return builder.toString();
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
    public String expressionString(Enums.ExprToStr t) {
        String expr;
        switch (t) {
            case SYMBOLIC_VALUE:
                expr = descriptor.getSymbolicValue();
                break;
            case VARIABLE_ID:
                expr = descriptor.getVariableId();
                break;
            default:
                throw new IllegalArgumentException("Such ExprToStr type (" + t.name() + ") isn't supported");
        }
        if (locationType == Enums.Location.ARRAY) {
            expr += "[" + index.expressionString(t) + "]";
        }
        return expr;
    }
    
    @Override
    public HashSet<String> generateUsedVariables() {
        HashSet<String> result = new HashSet<String>();
        result.add(descriptor.getVariableId());
        if (index != null) {
            result.addAll(index.generateUsedVariables());
        }
        return result;
    }
    
    @Override
    public boolean containsMethodCall() {
        if (isArray()) {
            return index.containsMethodCall();
        }
        else {
            return false;
        }
    }
    
    @Override
    public List<ExpressionASTNode> allSubexpressions() {
        if (isArray()) {
            List<ExpressionASTNode> res = new ArrayList<ExpressionASTNode>();
            res.addAll(index.allSubexpressions());
            if (!containsMethodCall()) {
                res.add(this);
            }
            return res;
        }
        return new ArrayList<ExpressionASTNode>();
    }

    @Override
    public void optimizeSubexpressions(Map<String, LocationASTNode> exprToTemp) {
        if (index != null) {
            LocationASTNode loc = exprToTemp.get(index.expressionString(Enums.ExprToStr.SYMBOLIC_VALUE));
            if (loc != null) {
                index = loc;
            } else {
                index.optimizeSubexpressions(exprToTemp);
            }
        }
    }
}
