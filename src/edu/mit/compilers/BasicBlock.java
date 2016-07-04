package edu.mit.compilers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.mit.compilers.astnode.AssignStmtASTNode;
import edu.mit.compilers.astnode.BlockASTNode;
import edu.mit.compilers.astnode.ExpressionASTNode;
import edu.mit.compilers.utils.Assertion;
import edu.mit.compilers.utils.Enums;
import edu.mit.compilers.utils.Enums.ExprToStr;
import edu.mit.compilers.utils.Strings;

public class BasicBlock {
    private List<BasicBlock> predecessors = new ArrayList<BasicBlock>();
    private List<BasicBlock> successors = new ArrayList<BasicBlock>();
    
    private List<AssignStmtASTNode> assignStmts = new ArrayList<AssignStmtASTNode>();
    private List<ExpressionASTNode> expressions = new ArrayList<ExpressionASTNode>();
    
    private List< Set<String> > lineKillSet = null;
    private List< Set<String> > lineGenSet = null;
    private List< Set<String> > avaialbleBefore = null;
    
    private Set< BasicBlock > nodes = null;

    private Set< String > gen = null;
    private Set< String > kill = null;
    
    private Set< String > in = new HashSet<String>();
    private Set< String > out = new HashSet<String>();
    
    private Set< String > use = null;
    private Set< String > def = null;
    
    private Set< String > liveIn = new HashSet<String>();
    private Set< String > liveOut = new HashSet<String>();
    
    private BlockASTNode blockASTNode;

    private Set<ExpressionASTNode> whileConditions = new HashSet<ExpressionASTNode>();

    public BasicBlock() {
        blockASTNode = null;
    }

    public BasicBlock(BlockASTNode blockASTNode) {
        this.blockASTNode = blockASTNode;
    }

    public void addWhileCondition(ExpressionASTNode expression) {
        addExpression(expression);
        whileConditions.add(expression);
    }

    public void addExpression(ExpressionASTNode expression) {
        Assertion.check(gen == null);
        expressions.add(expression);
        assignStmts.add(null);
    }
    
    public void addAssignStmt(AssignStmtASTNode statement) {
        Assertion.check(gen == null);
        expressions.add(statement.getExpression());
        assignStmts.add(statement);
    }

    public void addSuccessor(BasicBlock basicBlock) {
        Assertion.check(gen == null);
        successors.add(basicBlock);
        basicBlock.predecessors.add(this);
    }
    
    public void generateGenAndKill(Set<ExpressionASTNode> allExpressions, List<LocalDescriptor> globalDescriptors) {
        gen = new HashSet<>();
        kill = new HashSet<>();
        lineGenSet = new ArrayList<>();
        lineKillSet = new ArrayList<>();
        for (int i = 0; i < expressions.size(); ++i) {
            Set<String> genSet = new HashSet<>();
            Set<String> killSet = new HashSet<>();
            boolean hasMethod = false;

            if (assignStmts.get(i) != null) {
                for (ExpressionASTNode subexpr : assignStmts.get(i).allSubexpressions()) {
                    genSet.add(subexpr.expressionString(Enums.ExprToStr.VARIABLE_ID));
                }
                if (assignStmts.get(i).getLocation().expressionString(ExprToStr.VARIABLE_ID).contains("__method__")) {
                    hasMethod = true;
                }
            }
            for (ExpressionASTNode subexpr : expressions.get(i).allSubexpressions()) {
                genSet.add(subexpr.expressionString(Enums.ExprToStr.VARIABLE_ID));
            }
            
            if (expressions.get(i).expressionString(ExprToStr.VARIABLE_ID).contains("__method__")) {
                hasMethod = true;
            }
            
            lineGenSet.add(genSet);
            gen.addAll(genSet);

            Set<String> killerVars = new HashSet<>();
            if (hasMethod) {
                for (LocalDescriptor descriptor : globalDescriptors) {
                    killerVars.add(descriptor.getVariableId());
                }
            }
            if (assignStmts.get(i) != null) {
                killerVars.add(assignStmts.get(i).getLocation().getDescriptor().getVariableId());
            }
            for (String killer : killerVars) {
                for (ExpressionASTNode expr : allExpressions) {
                    if (expr.generateUsedVariables().contains(killer)) {
                        killSet.add(expr.expressionString(Enums.ExprToStr.VARIABLE_ID));
                    }
                }
            }
            lineKillSet.add(killSet);
            kill.addAll(killSet);
            gen.removeAll(killSet);
        }
    }
    
    public void generateUseAndDef(HashSet<String> globalVariables) {
        use = new HashSet<>();
        def = new HashSet<>();
        for (int i = expressions.size() - 1; i >= 0; i--) {
            use.addAll(expressions.get(i).generateUsedVariables());
            
            if (expressions.get(i).containsMethodCall()) {
                use.addAll(globalVariables);
            }
            
            if (assignStmts.get(i) != null) {
                String currDef = assignStmts.get(i).getLocation().getDescriptor().getVariableId();
                def.add(currDef);
                use.remove(currDef);
                
                if (assignStmts.get(i).getLocation().isArray()) {
                    use.addAll(assignStmts.get(i).getLocation().getIndex().generateUsedVariables());
//                    System.out.println(assignStmts.get(i).getLocation().getIndex().generateUsedVariables());
                }
            }
        }
//        System.out.println(use.toString());
//        System.out.println(def.toString());
    }
    
    public List<Set<String>> livenessAtStatements(HashSet<String> globalVariables) {
        List<Set<String>> concurances = new ArrayList<Set<String>>();
        HashSet<String> curr = new HashSet<String>(liveOut);
        concurances.add(new HashSet<>(curr));
        
        Set<String> next = new HashSet<>();
        for (int i = expressions.size() - 1; i >= 0; i--) {
            next = new HashSet<>(curr);
            curr.addAll(expressions.get(i).generateUsedVariables());
            
            if (expressions.get(i).containsMethodCall()) {
                curr.addAll(globalVariables);
            }
            
            if (assignStmts.get(i) != null) {
                String currDef = assignStmts.get(i).getLocation().getDescriptor().getVariableId();
                if (!next.contains(currDef) && !assignStmts.get(i).getLocation().isArray()) {
                    assignStmts.get(i).setSkip();
                }
                curr.remove(currDef);
                
                if (assignStmts.get(i).getLocation().isArray()) {
                    curr.addAll(assignStmts.get(i).getLocation().getIndex().generateUsedVariables());
                }
                concurances.add(new HashSet<>(curr));
            }
            else {
                concurances.add(new HashSet<>(curr));
            }
        }
        return concurances;
    }
    
    public void generateAvailabilitySets() {
        Assertion.check(avaialbleBefore == null);
        avaialbleBefore = new ArrayList<>();
        
        Set<String> currSet = new HashSet<>(in);
        for (int i = 0; i < expressions.size(); ++i) {
            avaialbleBefore.add(new HashSet<>(currSet));
            currSet.addAll(lineGenSet.get(i));
            currSet.removeAll(lineKillSet.get(i));
        }
    }

    public Set<ExpressionASTNode> getAllExpressions() {
        Set<ExpressionASTNode> all = new HashSet<ExpressionASTNode>();
        for (int i = 0; i < expressions.size(); ++i) {
            if (assignStmts.get(i) != null) {
                all.addAll(assignStmts.get(i).allSubexpressions());
            } else if (expressions.get(i) != null) {
                all.addAll(expressions.get(i).allSubexpressions());
            }
        }
        return all;
    }
    
    public Set<BasicBlock> getNodes() {
        if (nodes == null) {
            nodes = new HashSet<>();
        } else {
            return nodes;
        }
        nodes.add(this);
        for (BasicBlock block : successors) {
            nodes.addAll(block.getNodes());
        }
        
        return nodes;
    }
    
    public Set<BasicBlock> getPredecessors() {
        return new HashSet<BasicBlock>(this.predecessors);
    }

    public List<BasicBlock> getSuccessors() {
        return new ArrayList<BasicBlock>(this.successors);
    }

    public BlockASTNode getBlockASTNode() {
        return blockASTNode;
    }

    public Set<String> getGen() {
        return new HashSet<String>(this.gen);
    }
    
    public Set<String> getKill() {
        return new HashSet<String>(this.kill);
    }
    
    public Set<String> getIn() {
        return new HashSet<String>(this.in);
    }
    
    public Set<String> getOut() {
        return new HashSet<String>(this.out);
    }
    
    public Set<String> getUse() {
        return new HashSet<String>(this.use);
    }
    
    public Set<String> getDef() {
        return new HashSet<String>(this.def);
    }
    
    public Set<String> getLiveIn() {
        return new HashSet<String>(this.liveIn);
    }
    
    public Set<String> getLiveOut() {
        return new HashSet<String>(this.liveOut);
    }

    public List<AssignStmtASTNode> getAssignStmts() {
        return assignStmts;
    }

    public List<ExpressionASTNode> getExpressions() {
        return expressions;
    }

    public Set<ExpressionASTNode> getWhileConditions() {
        return whileConditions;
    }

    public void setIn(Set<String> in) {
        this.in = new HashSet<String>(in);
    }
    
    public void setOut(Set<String> out) {
        this.out = new HashSet<String>(out);
    }
    
    public void setLiveIn(Set<String> in) {
        this.liveIn = new HashSet<String>(in);
    }
    
    public void setLiveOut(Set<String> out) {
        this.liveOut = new HashSet<String>(out);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BLOCK: " + (blockASTNode == null ? "null" : blockASTNode.getName()));
        builder.append("\\lGEN: ");
        for (String expr : gen) {
            builder.append(Strings.escape(expr) + ", ");
        }
        builder.append("\\lKILL: ");
        for (String expr : kill) {
            builder.append(Strings.escape(expr) + ", ");
        }
        builder.append("\\lIN: ");
        for (String expr : in) {
            builder.append(Strings.escape(expr) + ", ");
        }
        builder.append("\\lOUT: ");
        for (String expr : out) {
            builder.append(Strings.escape(expr) + ", ");
        }
        builder.append("\\l\\l");
        for (int i = 0; i < assignStmts.size(); ++i) {
            if (assignStmts.get(i) != null) {
                builder.append(assignStmts.get(i).getLine() + ": " + Strings.escape(assignStmts.get(i).assignStmtString(Enums.ExprToStr.VARIABLE_ID)) + "\\l");
            } else if (expressions.get(i) != null) {
                builder.append(expressions.get(i).getLine() + ": " + Strings.escape(expressions.get(i).expressionString(Enums.ExprToStr.VARIABLE_ID)) + "\\l");
            } else {
                builder.append("null\n");
            }
        }
        return builder.toString();
    }

    public void removePredecessor(BasicBlock pred) {
        predecessors.remove(pred);
    }
}
