package edu.mit.compilers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.mit.compilers.astnode.*;
import edu.mit.compilers.utils.Assertion;
import edu.mit.compilers.utils.Enums;

public class DataflowVisitor {

    private Map<String, BasicBlock> methodCFGs;

    private BasicBlock currentBasicBlock = null;
    private BasicBlock breakBasicBlock = null;
    private BasicBlock continueBasicBlock = null;

    private GlobalSymbolTable globalSymbolTable;
    private SymbolTableStack symbolTableStack;
    
    private int cycleCounter;
    private Map<String, Integer> variableAccessCount; 

    public DataflowVisitor() {
        methodCFGs = new HashMap<String, BasicBlock>();
        globalSymbolTable = new GlobalSymbolTable(new HashSet<String>());
        symbolTableStack = new SymbolTableStack();
        symbolTableStack.push(globalSymbolTable);
    }

    public void visit(ProgramASTNode programASTNode) {
        cycleCounter = 0;
        variableAccessCount = new HashMap<String, Integer>();
        for (FieldDeclASTNode node : programASTNode.getFields()) {
            node.accept(this);
        }
        for (MethodDeclASTNode node : programASTNode.getMethods()) {
            node.accept(this); // will modify methodCFGs
        }
        
        // clean up hanging nodes
        for (String methodName : methodCFGs.keySet()) {
            Set<BasicBlock> nodes = methodCFGs.get(methodName).getNodes();
            for (BasicBlock block : nodes) {
                for (BasicBlock pred : new HashSet<>(block.getPredecessors())) {
                    if (!nodes.contains(pred)) {
                        block.removePredecessor(pred);
                    }
                }
            }
        }
    }

    public void visit(MethodDeclASTNode methodDeclASTNode) {
        List<String> paramNames = methodDeclASTNode.getParameterNames();
        List<Enums.Type> paramTypes = methodDeclASTNode.getParameterTypes();

        Set<String> symbolSet = new HashSet<String>();
        FieldSymbolTable<LocalDescriptor> paramSymbolTable = new FieldSymbolTable<LocalDescriptor>(symbolSet);
        for (int i = 0; i < paramNames.size(); i++) {
            paramSymbolTable.add(paramNames.get(i), new LocalDescriptor(paramNames.get(i), paramTypes.get(i)));
        }
        
        symbolTableStack.push(paramSymbolTable);

        Assertion.check(currentBasicBlock == null);
        Assertion.check(breakBasicBlock == null);
        Assertion.check(continueBasicBlock == null);

        currentBasicBlock = new BasicBlock(methodDeclASTNode.getBlock());
        methodCFGs.put(methodDeclASTNode.getMethodName(), currentBasicBlock);

        methodDeclASTNode.getBlock().accept(this);
        currentBasicBlock = null;
        
        symbolTableStack.pop();
    }

    public void visit(BlockASTNode blockASTNode) {
        Set<String> symbolSet;
        if (blockASTNode.isLevel1Block()) {
            symbolSet = symbolTableStack.top().getSymbolSetRef();
        } else {
            symbolSet = new HashSet<String>();
        }
        FieldSymbolTable<LocalDescriptor> localSymbolTable = new FieldSymbolTable<LocalDescriptor>(symbolSet);
        symbolTableStack.push(localSymbolTable);

        for (ASTNode statement : blockASTNode.getStatements()) {
            statement.accept(this);
        }

        symbolTableStack.pop();
    }

    public void visit(ExpressionASTNode expressionASTNode) {
        currentBasicBlock.addExpression(expressionASTNode);
        
        updateExpressionAccessCounters(expressionASTNode);
    }

    public void visit(AssignStmtASTNode assignStmtASTNode) {
        currentBasicBlock.addAssignStmt(assignStmtASTNode);
        
        updateExpressionAccessCounters(assignStmtASTNode.getExpression());
        String var = assignStmtASTNode.getLocation().expressionString(Enums.ExprToStr.VARIABLE_ID);
        if (!variableAccessCount.containsKey(var)) {
            variableAccessCount.put(var, 0);
        }
        variableAccessCount.put(var, variableAccessCount.get(var) + 1 + cycleCounter * 10);
    }

    public void visit(IfASTNode ifASTNode) {
        BasicBlock ifRoot = currentBasicBlock;
        BasicBlock ifEnd = new BasicBlock(ifRoot.getBlockASTNode());

        ifASTNode.getCondition().accept(this);

        currentBasicBlock = new BasicBlock(ifASTNode.getBlockTrue());
        ifRoot.addSuccessor(currentBasicBlock);
        ifASTNode.getBlockTrue().accept(this);
        currentBasicBlock.addSuccessor(ifEnd);

        if (ifASTNode.getBlockFalse() != null) {
            currentBasicBlock = new BasicBlock(ifASTNode.getBlockFalse());
            ifRoot.addSuccessor(currentBasicBlock);
            ifASTNode.getBlockFalse().accept(this);
            currentBasicBlock.addSuccessor(ifEnd);
        } else {
            ifRoot.addSuccessor(ifEnd);
        }

        currentBasicBlock = ifEnd;
    }

    public void visit(ForASTNode forASTNode) {
        // UNDO at the end
        
        BasicBlock prevBreak = breakBasicBlock;
        BasicBlock prevContinue = continueBasicBlock;

        LocationASTNode forLoc = forASTNode.getLocation();
        ExpressionASTNode forInit = forASTNode.getInitialValue();
        ExpressionASTNode forStep = forASTNode.getIncrementStep();
        if (forStep == null) {
            forStep = new IntLiteralASTNode(1);
        }
        ExpressionASTNode forFinal = forASTNode.getFinalValue();
        BlockASTNode forBlock = forASTNode.getBlock();
        LocationASTNode tmp = new LocationASTNode("__for", forFinal.getType());
//        variableAccessCount.put(tmp.getDescriptor().getVariableId(), 1 + cycleCounter * 10);
//        currentBasicBlock.addAssignStmt(new AssignStmtASTNode("=", forLoc, forInit));
        AssignStmtASTNode initAssign = new AssignStmtASTNode("=", forLoc, forInit);
        initAssign.accept(this);
//        currentBasicBlock.addAssignStmt(new AssignStmtASTNode("=", tmp, forFinal));
        AssignStmtASTNode tmpAssign = new AssignStmtASTNode("=", tmp, forFinal);
        tmpAssign.accept(this);

        cycleCounter++; // undo at the end
        
        BasicBlock forRoot = new BasicBlock();
        BasicBlock forEnd = new BasicBlock(currentBasicBlock.getBlockASTNode());
        BasicBlock forInc = new BasicBlock();
        // Add i<n for forRoot
        ExpressionASTNode condition = new RelationalOpASTNode(Enums.RelationalOp.LT, forLoc, tmp, Enums.Type.BOOL_SINGLE);
        forRoot.addExpression(condition);
        // Generate i=i+1 for focInc
        ExpressionASTNode incExpr = new ArithmeticOpASTNode(Enums.ArithmeticOp.PLUS, forLoc, forStep, Enums.Type.INT_SINGLE);
        AssignStmtASTNode incAssign = new AssignStmtASTNode("=", forLoc, incExpr);
        forInc.addAssignStmt(incAssign);

        currentBasicBlock.addSuccessor(forRoot);
        currentBasicBlock = forRoot;

        continueBasicBlock = forInc;
        breakBasicBlock = forEnd;

        currentBasicBlock = new BasicBlock(forBlock);
        forRoot.addSuccessor(currentBasicBlock);

        forRoot.addSuccessor(forEnd);
        forBlock.accept(this);
        currentBasicBlock.addSuccessor(forInc);
        forInc.addSuccessor(forRoot);

        currentBasicBlock = forEnd;

        continueBasicBlock = prevContinue;
        breakBasicBlock = prevBreak;
        
        updateExpressionAccessCounters(condition);
        updateExpressionAccessCounters(incExpr);
        cycleCounter--;
    }

    public void visit(WhileASTNode whileASTNode) {
        // UNDO at the end
        cycleCounter++;
        BasicBlock prevBreak = breakBasicBlock;
        BasicBlock prevContinue = continueBasicBlock;

        BasicBlock whileRoot = new BasicBlock();
        BasicBlock whileEnd = new BasicBlock(currentBasicBlock.getBlockASTNode());
        breakBasicBlock = whileEnd;
        continueBasicBlock = whileRoot;

        currentBasicBlock.addSuccessor(whileRoot);

        whileRoot.addWhileCondition(whileASTNode.getCondition());
        updateExpressionAccessCounters(whileASTNode.getCondition());

        currentBasicBlock = new BasicBlock(whileASTNode.getBlock());
        whileRoot.addSuccessor(currentBasicBlock);

        whileASTNode.getBlock().accept(this);
        if (currentBasicBlock != null) {
            currentBasicBlock.addSuccessor(whileRoot);
        }

        whileRoot.addSuccessor(whileEnd);

        currentBasicBlock = whileEnd;

        continueBasicBlock = prevContinue;
        breakBasicBlock = prevBreak;
        cycleCounter--;
    }

    public void visit(BreakASTNode breakASTNode) {
        currentBasicBlock.addSuccessor(breakBasicBlock);
        currentBasicBlock = new BasicBlock();
    }

    public void visit(ContinueASTNode continueASTNode) {
        currentBasicBlock.addSuccessor(continueBasicBlock);
        currentBasicBlock = new BasicBlock();
    }

    public void visit(ReturnASTNode returnASTNode) {
        // dead ends are returns
        if (returnASTNode.getExpression() != null) {
            currentBasicBlock.addExpression(returnASTNode.getExpression());
            updateExpressionAccessCounters(returnASTNode.getExpression());
        }
    }

    public void visit(FieldDeclASTNode fieldDeclASTNode) {
    }

    public void visit(SingleFieldDeclASTNode singleFieldDeclASTNode) {
//        Enums.Type type = singleFieldDeclASTNode.getType();
//        String name = singleFieldDeclASTNode.getName();
//        int length = singleFieldDeclASTNode.getLength();
//
//        SymbolTable<LocalDescriptor> symbolTable = symbolTableStack.top();
//        LocalDescriptor descriptor = new LocalDescriptor(name, type, length);
//        symbolTable.add(name, descriptor);
    }

    public void visit(CalloutDeclASTNode calloutDeclASTNode) {
        // wohoo!
    }

    public Map<String, BasicBlock> getMap() {
        return methodCFGs;
    }
    
//    public GlobalSymbolTable getGlobalsSymbolTable() {
//        return globalSymbolTable;
//    }
    
    public Map<String, Integer> getVariableAccessCount() {
        return variableAccessCount;
    }
    
    private void updateExpressionAccessCounters(ExpressionASTNode expression) {
        for (String var : expression.generateUsedVariables()) {
            if (!variableAccessCount.containsKey(var)) {
                variableAccessCount.put(var, 0);
            }
            variableAccessCount.put(var, variableAccessCount.get(var) + 1 + cycleCounter * 10);
        }
    }
}
