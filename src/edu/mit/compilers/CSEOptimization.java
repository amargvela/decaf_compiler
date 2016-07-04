package edu.mit.compilers;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

import edu.mit.compilers.astnode.*;
import edu.mit.compilers.utils.Enums;
import edu.mit.compilers.utils.StateAccumulator;

public class CSEOptimization {

    private static final boolean DEBUG = true;
    private static int tempCounter = 0;

    public static void analyze(StateAccumulator accumulator) throws FileNotFoundException {
        Map<String, BasicBlock> cfgs = accumulator.dataflowVisitor.getMap();
        
        for (String methodName : cfgs.keySet()) {
            Set<ExpressionASTNode> allExpressions = new HashSet<>();
            for (BasicBlock block : cfgs.get(methodName).getNodes()) {
                allExpressions.addAll(block.getAllExpressions());
            }
            for (BasicBlock block : cfgs.get(methodName).getNodes()) {
                block.generateGenAndKill(new HashSet<ExpressionASTNode>(allExpressions), accumulator.globalSymbolTable.getAllDescriptors());
            }

            BasicBlock entryNode = cfgs.get(methodName);
            
            Set<String> allExpressionsStr = new HashSet<>();
            for (ExpressionASTNode expr : allExpressions) {
                allExpressionsStr.add(expr.expressionString(Enums.ExprToStr.VARIABLE_ID));
            }
            generateInsAndOuts(entryNode, allExpressionsStr);
            
            for (BasicBlock block : cfgs.get(methodName).getNodes()) {
                block.generateAvailabilitySets();
            }

            Map<String, LocationASTNode> allExprToTemp = new HashMap<String, LocationASTNode>();
            Map<String, String> exprStrMap = new HashMap<String, String>();
            for (ExpressionASTNode expr : allExpressions) {
                String exprStr = expr.expressionString(Enums.ExprToStr.VARIABLE_ID);
                exprStrMap.put(exprStr, expr.expressionString(Enums.ExprToStr.SYMBOLIC_VALUE));
                if (!allExprToTemp.containsKey(exprStr) && !(expr instanceof MethodCallASTNode)) {
                    LocationASTNode loc = new LocationASTNode(nextTemp(), expr.getType());
                    allExprToTemp.put(exprStr, loc);
                    cfgs.get(methodName).getBlockASTNode().declareField(new SingleFieldDeclASTNode(expr.getType(), loc.getName()));
                }
            }
            analyzeBasicBlock(entryNode, allExprToTemp, exprStrMap, accumulator.globalSymbolTable.getAllDescriptors());
        }

        for (String methodName : cfgs.keySet()) {
            printCFG(cfgs.get(methodName), methodName);
        }
    }

    private static Set<BasicBlock> isAnalyzed = new HashSet<BasicBlock>();

    private static void analyzeBasicBlock(
            BasicBlock basicBlock,
            Map<String, LocationASTNode> allExprToTemp,
            Map<String, String> exprStrMap,
            List<LocalDescriptor> globalDescriptors) {
        // analyze each basic block only once
        if (isAnalyzed.contains(basicBlock)) {
            return;
        } else {
            isAnalyzed.add(basicBlock);
        }
        Map<String, LocationASTNode> exprToTemp = new HashMap<String, LocationASTNode>();
        // put all available expressions in temporaries
        for (String exprStr : basicBlock.getIn()) {
            String exprStrSymbolic = exprStrMap.get(exprStr);
            LocationASTNode temp = allExprToTemp.get(exprStr);
            exprToTemp.put(exprStrSymbolic, temp);
        }
        List<AssignStmtASTNode> assignments = basicBlock.getAssignStmts();
        List<ExpressionASTNode> expressions = basicBlock.getExpressions();
        Set<ExpressionASTNode> whileConditions = basicBlock.getWhileConditions();
        for (int i = 0; i < assignments.size(); i++) {
            AssignStmtASTNode stmt = assignments.get(i);
            ExpressionASTNode expr = expressions.get(i);
            // Skip while conditions
            if (whileConditions.contains(expr)) {
                continue;
            }
            // If method call happened, invalidate all globals and skip optimizations
            if (expr.containsMethodCall()) {
                for (LocalDescriptor descriptor : globalDescriptors) {
                    descriptor.setSymbolicValue();
                }
                continue;
            }
            // add all sub expressions in exprToTemp
            List<ExpressionASTNode> subExprs = expr.allSubexpressions();
            List<String> exprStrSymbolics = new ArrayList<String>();
            List<String> exprStrs = new ArrayList<String>();
            for (ExpressionASTNode subExpr : expr.allSubexpressions()) {
                exprStrSymbolics.add(subExpr.expressionString(Enums.ExprToStr.SYMBOLIC_VALUE));
                exprStrs.add(subExpr.expressionString(Enums.ExprToStr.VARIABLE_ID));
            }
            for (int j = 0; j < subExprs.size(); j++) {
                ExpressionASTNode subExpr = subExprs.get(j);
                String exprStrSymbolic = exprStrSymbolics.get(j);
                if (!exprToTemp.containsKey(exprStrSymbolic)) {
                    String exprStr = exprStrs.get(j);
                    LocationASTNode loc = allExprToTemp.get(exprStr);
                    if (loc == null) {
                        throw new IllegalStateException("analyzeBasicBlock: Location can't be null. " + exprStr + " isn\'t in allExprToTemp");
                    }
                    subExpr.optimizeSubexpressions(exprToTemp);
                    exprStrSymbolic = subExpr.expressionString(Enums.ExprToStr.SYMBOLIC_VALUE);
                    exprToTemp.put(exprStrSymbolic, loc);
                    AssignStmtASTNode subStmt = new AssignStmtASTNode("=", loc, subExpr);
                    assignments.add(i, subStmt);
                    expressions.add(i, subExpr);
                    i++;
                    if (basicBlock.getBlockASTNode() != null) {
                        boolean match = false;
                        if (stmt != null) {
                            match = basicBlock.getBlockASTNode().insertStmtBefore(stmt, subStmt);
                        }
                        if (!match) {
                            basicBlock.getBlockASTNode().insertStmtBefore(expr, subStmt);
                        }
                    }
                }
            }
            // optimize sub expressions
            if (stmt != null) {
                stmt.optimizeSubexpressions(exprToTemp);
            } else {
                expr.optimizeSubexpressions(exprToTemp);
            }
        }

        for (BasicBlock nextBlock : basicBlock.getSuccessors()) {
            analyzeBasicBlock(nextBlock, allExprToTemp, exprStrMap, globalDescriptors);
        }
    }

    private static String nextTemp() {
        return "__tmp_" + tempCounter++;
    }

    private static void generateInsAndOuts(BasicBlock entryNode, Set<String> allExpressions) {
        Set<BasicBlock> allNodes = new HashSet<BasicBlock>();
        findAllNodes(entryNode, allNodes);

        for (BasicBlock node : allNodes) {
            node.setOut(allExpressions);
        }
        entryNode.setIn(new HashSet<String>());
        entryNode.setOut(entryNode.getGen());
        Set<BasicBlock> changed = new HashSet<BasicBlock>(allNodes);
        changed.remove(entryNode);

        while (!changed.isEmpty()) {
            BasicBlock curr = null;
            for (BasicBlock node : changed) {
                curr = node;
                break;
            }
            changed.remove(curr);

            Set<String> newIn = new HashSet<String>(allExpressions);
            for (BasicBlock pred: curr.getPredecessors()) {
                if (pred.getPredecessors().size() > 0 || pred.equals(entryNode)) {
                    newIn.retainAll(pred.getOut());
                }
            }
            curr.setIn(newIn);

            Set<String> newOut = curr.getIn();
            newOut.removeAll(curr.getKill());
            newOut.addAll(curr.getGen());
            boolean outChanged = !(curr.getOut().containsAll(newOut) && newOut.containsAll(curr.getOut()));
            curr.setOut(newOut);

            if (outChanged) {
                changed.addAll(curr.getSuccessors());
            }
        }
    }

    private static void findAllNodes(BasicBlock entryNode, Set<BasicBlock> allNodes) {
        if (allNodes.contains(entryNode)) {
            return;
        }
        allNodes.add(entryNode);
        for (BasicBlock succ : entryNode.getSuccessors()) {
            findAllNodes(succ, allNodes);
        }
    }

    private static void printCFG(BasicBlock basicBlock, String methodName) throws FileNotFoundException {
        if (!DEBUG) {
            return;
        }

        int N = 0;
        PrintWriter writer = new PrintWriter(methodName + ".dot");
        Map<BasicBlock, Integer> blockIdMap = new HashMap<BasicBlock, Integer>();

        writer.write("digraph " + methodName + " {\n");
        for (BasicBlock node : basicBlock.getNodes()) {
            blockIdMap.put(node, N);
            writer.write(String.format("    %d [shape=box, label=\"%s\"];\n", N, node.toString()));
            N++;
        }
        writer.write("\n");

        for (BasicBlock node : basicBlock.getNodes()) {
            for (BasicBlock next : node.getSuccessors()) {
                writer.write(String.format("    %d -> %d;\n", blockIdMap.get(node), blockIdMap.get(next)));
            }
        }

        writer.write("}\n");

        writer.close();
    }
}
