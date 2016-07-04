package edu.mit.compilers;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.mit.compilers.astnode.*;
import edu.mit.compilers.utils.Assertion;
import edu.mit.compilers.utils.Enums;
import edu.mit.compilers.utils.StateAccumulator;

public class SemanticCheckVisitor {

    private PrintStream out;
    private StateAccumulator accumulator;
    // Symbol Tables
    private SymbolTable<MethodDescriptor> methodSymbolTable;
    private SymbolTable<CalloutDescriptor> calloutSymbolTable;
//    private FieldSymbolTable<LocalDescriptor> globalSymbolTable;
    private GlobalSymbolTable globalSymbolTable;

    private SymbolTableStack symbolTableStack;
    
    private String currentMethodName;
    private int InsideLoopsNumber;
    
    private void reportError(int line, int col, String err) {
        out.println(line + ":" + col + "\t" + err);
    }

    public SemanticCheckVisitor(PrintStream out, StateAccumulator accumulator) {
        this.out = out;
        this.accumulator = accumulator;
        Set<String> globalSet = new HashSet<>();
        methodSymbolTable = new SymbolTable<>(globalSet);
        calloutSymbolTable = new SymbolTable<>(globalSet);
//        globalSymbolTable = new FieldSymbolTable<>(globalSet);
        globalSymbolTable = new GlobalSymbolTable(globalSet);
        symbolTableStack = new SymbolTableStack();
        symbolTableStack.push(globalSymbolTable);
        currentMethodName = null;
        InsideLoopsNumber = 0;
    }
    
    // All visit methods
    public boolean visit(ProgramASTNode programASTNode) {
        boolean accept = true;

        boolean mainDeclared = false;
        for (MethodDeclASTNode node : programASTNode.getMethods()) {
            mainDeclared = mainDeclared || node.getMethodName().equals("main");
        }
        if (!mainDeclared) {
            reportError(programASTNode.getLine(), programASTNode.getColumn(), "Method 'main' not declared.");
            accept = false;
        }

        for (CalloutDeclASTNode node : programASTNode.getCallouts()) {
            accept = node.accept(this) && accept;
        }
        for (FieldDeclASTNode node : programASTNode.getFields()) {
            accept = node.accept(this) && accept;
        }
        for (MethodDeclASTNode node : programASTNode.getMethods()) {
            accept = node.accept(this) && accept;
        }
        return accept;
    }

    public boolean visit(ArithmeticOpASTNode arithmeticOpASTNode) {
        return visitBinaryOperation(arithmeticOpASTNode, Enums.Type.INT_SINGLE, Enums.Type.INT_SINGLE, Enums.getOpName(arithmeticOpASTNode.getOp()));
    }
    
    public boolean visit(RelationalOpASTNode relationalOpASTNode) {
        if (relationalOpASTNode.getOp() == Enums.RelationalOp.EQ ||
            relationalOpASTNode.getOp() == Enums.RelationalOp.NEQ) {
            return visitBinaryOperation(relationalOpASTNode, Enums.Type.EXPR, Enums.Type.BOOL_SINGLE, Enums.getOpName(relationalOpASTNode.getOp()));
        } else {
            return visitBinaryOperation(relationalOpASTNode, Enums.Type.INT_SINGLE, Enums.Type.BOOL_SINGLE, Enums.getOpName(relationalOpASTNode.getOp()));
        }
    }
    
    public boolean visit(LogicalOpASTNode logicalOpASTNode) {
        return visitBinaryOperation(logicalOpASTNode, Enums.Type.BOOL_SINGLE, Enums.Type.BOOL_SINGLE, Enums.getOpName(logicalOpASTNode.getOp()));
    }
    
    private boolean visitBinaryOperation(BinaryOpASTNode opASTNode, Enums.Type opType, Enums.Type retType, String opName) {
        boolean correct = true;
        
        ExpressionASTNode left = opASTNode.leftOperand();
        correct = left.accept(this) && correct;
        
        ExpressionASTNode right = opASTNode.rightOperand();
        correct = right.accept(this) && correct;
        
        if (!(left.getType() == Enums.Type.BOOL_SINGLE || left.getType() == Enums.Type.INT_SINGLE) ||
            !(right.getType() == Enums.Type.BOOL_SINGLE || right.getType() == Enums.Type.INT_SINGLE) ||
            left.getType() != right.getType() ||
            left.getType() != opType && opType != Enums.Type.EXPR) {
            
            if (left.getType() != Enums.Type.EXPR && right.getType() != Enums.Type.EXPR) {
                reportError(
                        opASTNode.getLine(),
                        opASTNode.getColumn(),
                        "Operands of operation '" +
                        opName + "' have incompatible types '" +
                        Enums.getTypeName(left.getType()) + "' and '" +
                        Enums.getTypeName(right.getType()) + "'");
            }
            correct = false;
            opASTNode.setType(Enums.Type.EXPR);
        } else {
            opASTNode.setType(retType);
        }
        return correct;
    }

    public boolean visit(ArrayLenASTNode arrayLenASTNode) {
        String name = arrayLenASTNode.getName();
        LocalDescriptor descriptor = symbolTableStack.getDescriptor(name);
        if (descriptor == null) {
            reportError(
                    arrayLenASTNode.getLine(),
                    arrayLenASTNode.getColumn(),
                    "Variable '" + name + "' is not declared");
            return false;
        }
        if (!Enums.isTypeArray(descriptor.getType())) {
            reportError(
                    arrayLenASTNode.getLine(),
                    arrayLenASTNode.getColumn(),
                    "Variable '" + name + "' is not an array");
            return false;
        }
        arrayLenASTNode.setLength(descriptor.getLength());
        return true;
    }

    public boolean visit(AssignStmtASTNode assignStmtASTNode) {
        boolean accept = true;
        
        LocationASTNode location = assignStmtASTNode.getLocation();
        ExpressionASTNode expression = assignStmtASTNode.getExpression();
        
        accept = location.accept(this) && accept;
        accept = expression.accept(this) && accept;

        if (accept) {
            Enums.Type expectedType = location.getType();
            Enums.Type actualType = expression.getType();
            if (expectedType != actualType) {
                accept = false;
                reportError(
                        expression.getLine(),
                        expression.getColumn(),
                        "Expected type '"+Enums.getTypeName(expectedType)+"', recieved '"+Enums.getTypeName(actualType)+"'"
                );
            } else if (expectedType == Enums.Type.BOOL_SINGLE && assignStmtASTNode.getAssignment() != Enums.Assignment.EQUALS) {
                accept = false;
                reportError(
                        assignStmtASTNode.getLine(),
                        assignStmtASTNode.getColumn(),
                        "You can not do += or -= operations on boolean"
                );
            }
        }
        return accept;
    }

    public boolean visit(BlockASTNode blockASTNode) {
        boolean accept = true;
        Set<String> symbolSet;
        if (blockASTNode.isLevel1Block()) {
            symbolSet = symbolTableStack.top().getSymbolSetRef();
        } else {
            symbolSet = new HashSet<String>();
        }
        FieldSymbolTable<LocalDescriptor> localSymbolTable = new FieldSymbolTable<LocalDescriptor>(symbolSet);
        symbolTableStack.push(localSymbolTable);
        for (FieldDeclASTNode field : blockASTNode.getFields()) {
            accept = field.accept(this) && accept;
        }
        for (ASTNode stmt : blockASTNode.getStatements()) {
            accept = stmt.accept(this) && accept;
        }
        symbolTableStack.pop();
        return accept;
    }

    public boolean visit(BoolLiteralASTNode boolLiteralASTNode) {
        return true;
    }

    public boolean visit(BreakASTNode breakASTNode) {
        boolean correct = true;
        
        if (InsideLoopsNumber <= 0) {
            correct = false;
            reportError(breakASTNode.getLine(), breakASTNode.getColumn(),
                    "continue statement must be inside for or while loop");
        }
        
        return correct;
    }

    public boolean visit(CalloutDeclASTNode calloutDeclASTNode) {
        String name = calloutDeclASTNode.getName();
        if (calloutSymbolTable.canAdd(name)) {
            calloutSymbolTable.add(name, new CalloutDescriptor(name));
            return true;
        }
        reportError(calloutDeclASTNode.getLine(), calloutDeclASTNode.getColumn(),
                "Callout '" + name + "' declared more than once.");
        return false;
    }

    public boolean visit(ContinueASTNode continueASTNode) {
        boolean correct = true;
        
        if (InsideLoopsNumber <= 0) {
            correct = false;
            reportError(continueASTNode.getLine(), continueASTNode.getColumn(),
                    "continue statement must be inside for or while loop");
        }
        
        return correct;
    }

    public boolean visit(ExpressionASTNode expressionASTNode) {
        ExpressionASTNode expression = expressionASTNode.getExpression();
        boolean accept = expression.accept(this);
        expressionASTNode.setType(expression.getType());
        return accept;
    }

    public boolean visit(FieldDeclASTNode fieldDeclASTNode) {
        boolean correct = true;
        for (SingleFieldDeclASTNode node : fieldDeclASTNode.getSingleFieldDecls()) {
            correct = node.accept(this) && correct;
        }
        return correct;
    }

    public boolean visit(ForASTNode forASTNode) {
        boolean correct = true;

        LocationASTNode location = forASTNode.getLocation();
        if (!location.accept(this)) {
            correct = false;
        }
        Enums.Type locationType = symbolTableStack.getDescriptor(location.getName()).getType();
        if (locationType != Enums.Type.INT_SINGLE) {
            correct = false;
            reportError(forASTNode.getLine(), forASTNode.getColumn(),
                    "Expected variable type 'int', received type '" + Enums.getTypeName(locationType)+"'");
        }
        
        ExpressionASTNode initialValue = forASTNode.getInitialValue();
        if (!initialValue.accept(this))
            correct = false;;
        if (initialValue.getType() != Enums.Type.INT_SINGLE) {
            correct = false;
            reportError(forASTNode.getLine(), forASTNode.getColumn(),
                    "Initial value of the for loop variable must evaluate to integer");
        }

        ExpressionASTNode finalValue = forASTNode.getFinalValue();
        if (!finalValue.accept(this))
            correct = false;
        if (finalValue.getType() != Enums.Type.INT_SINGLE) {
            correct = false;
            reportError(forASTNode.getLine(), forASTNode.getColumn(),
                    "Final value of the for loop variable must evaluate to integer");
        }

        IntLiteralASTNode step = forASTNode.getIncrementStep();
        if (step != null) {
            if (step.getValue() <= 0) {
                correct = false;
                reportError(forASTNode.getLine(), forASTNode.getColumn(), "Increment step must be a positive integer");
            }
        }
        
        InsideLoopsNumber++;
        if (!forASTNode.getBlock().accept(this))
            correct = false;
        InsideLoopsNumber--;

        return correct;
    }

    public boolean visit(IfASTNode ifASTNode) {
        boolean correct = true;
        ExpressionASTNode condition = ifASTNode.getCondition();
        
        correct = condition.accept(this) && correct;
        
        if (condition.getType() != Enums.Type.BOOL_SINGLE) {
            if (condition.getType() != Enums.Type.EXPR) {
                reportError(
                        ifASTNode.getLine(),
                        ifASTNode.getColumn(),
                        "If statement has to have expression of type '" + Enums.getTypeName(Enums.Type.BOOL_SINGLE) + "'" +
                        " instead of type '" + Enums.getTypeName(condition.getType()) + "'"
                );
            }
            correct = false;
        }
        
        correct = ifASTNode.getBlockTrue().accept(this) && correct;
        if (ifASTNode.hasElse()) {
            correct = ifASTNode.getBlockFalse().accept(this) && correct;
        }
        return correct;
    }

    public boolean visit(IntLiteralASTNode intLiteralASTNode) {
        // TODO(tsotne) check for overflow (be careful with minuses)
        return true;
    }

    public boolean visit(LocationASTNode locationASTNode) {
        boolean accept = true;
        LocalDescriptor descriptor = symbolTableStack.getDescriptor(locationASTNode.getName());
        if (descriptor == null) {
            reportError(
                    locationASTNode.getLine(),
                    locationASTNode.getColumn(),
                    "Variable '" + locationASTNode.getName() + "' is not declared"
            );
            return false;
        }
        if (locationASTNode.getLocationType() == Enums.Location.ARRAY) {
            ExpressionASTNode index = locationASTNode.getIndex();
            accept = index.accept(this) && accept;
            if (!Enums.isTypeArray(descriptor.getType())) {
                accept = false;
                reportError(index.getLine(), index.getColumn(), "This field is not an array");
            }
            if (index.getType() != Enums.Type.INT_SINGLE) {
                accept = false;
                if (index.getType() != Enums.Type.EXPR) {
                    reportError(
                            index.getLine(),
                            index.getColumn(),
                            "Array index must be of type INT, not type "+Enums.getTypeName(index.getType())
                    );
                }
            }
            if (accept) {
                locationASTNode.setType(Enums.convertTypeToSingle(descriptor.getType()));
            }
        } else {
            if (Enums.isTypeArray(descriptor.getType())) {
                accept = false;
                reportError(locationASTNode.getLine(), locationASTNode.getColumn(), "Array index not specified");
            }
            if (accept) {
                locationASTNode.setType(descriptor.getType());
            }
        }
        locationASTNode.setDescriptor(descriptor);
        return accept;
    }

    public boolean visit(MethodCallASTNode methodCallASTNode) {
        String name = methodCallASTNode.getMethodName();
        List<MethodCallParam> params = methodCallASTNode.getParams();
        if (!methodSymbolTable.exists(name) && !calloutSymbolTable.exists(name) ) {
            reportError(
                    methodCallASTNode.getLine(),
                    methodCallASTNode.getColumn(),
                    "Method/Callout '" + name + "' used, but not declared."
                    );
            return false;
        }
        if (calloutSymbolTable.exists(name)) {
            // If it's callout, just verify the parameters recursively
            for(MethodCallParam param : params) {
                if (param.getType() == Enums.Type.EXPR && !param.getExpr().accept(this)) {
                    return false;
                }
                if (param.getType() == Enums.Type.STR) {
                    accumulator.addStrliteral(param.getLiteral());
                }
            }
            methodCallASTNode.setType(Enums.Type.INT_SINGLE);
            return true;
        }

        // The rest handles Method Call case

        MethodDescriptor descriptor = methodSymbolTable.get(name);
        List<Enums.Type> parameterTypes = descriptor.getParameterTypes();

        if (parameterTypes.size() != params.size()) {
            reportError(
                    methodCallASTNode.getLine(),
                    methodCallASTNode.getColumn(),
                    "Method '" + name + "' requires " + parameterTypes.size() + " arguments; " + params.size() + " provided");
            return false;
        }
        boolean accept = true;
        for (int i = 0; i < parameterTypes.size(); ++i) {
            if (params.get(i).getType() != Enums.Type.STR) {
                accept = params.get(i).getExpr().accept(this) && accept;
                params.get(i).setType(params.get(i).getExpr().getType());
            }
            if (parameterTypes.get(i) != params.get(i).getType()) {
                if (params.get(i).getType() != Enums.Type.EXPR)
                    reportError(
                            methodCallASTNode.getLine(),
                            methodCallASTNode.getColumn(),
                            "Argument " + (i+1) + " of method " + name + " must be of type '" +
                                    Enums.getTypeName(parameterTypes.get(i)) + "'; '" +
                                    Enums.getTypeName(params.get(i).getType()) + "' provided");
                accept = false;
            }
        }
        methodCallASTNode.setType(descriptor.getType());
        return accept;
    }

    public boolean visit(MethodDeclASTNode methodDeclASTNode) {
        boolean accept = true;

        // Extract variables from methodDeclASTNode
        int line = methodDeclASTNode.getLine();
        int column = methodDeclASTNode.getColumn();
        String name = methodDeclASTNode.getMethodName();
        Enums.Type type = methodDeclASTNode.getMethodType();
        List<String> paramNames = methodDeclASTNode.getParameterNames();
        List<Enums.Type> paramTypes = methodDeclASTNode.getParameterTypes();
        
        if (name.equals("main")) {
            if (paramNames.size() > 0) {
                accept = false;
                reportError(
                        methodDeclASTNode.getLine(),
                        methodDeclASTNode.getColumn(),
                        "Method 'main' should not have parameters");
            }
            if (type != Enums.Type.VOID) {
                accept = false;
                reportError(
                        methodDeclASTNode.getLine(), methodDeclASTNode.getColumn(),
                        "Method 'main' should have return type " + Enums.getTypeName(Enums.Type.VOID));
            }
        }
        
        Set<String> symbolSet = new HashSet<String>();
        FieldSymbolTable<LocalDescriptor> paramSymbolTable = new FieldSymbolTable<LocalDescriptor>(symbolSet);
        for (int i = 0; i < paramNames.size(); i++) {
            // Check if such parameter name already exists
            if (paramSymbolTable.canAdd(paramNames.get(i))) {
                paramSymbolTable.add(paramNames.get(i), new LocalDescriptor(paramNames.get(i), paramTypes.get(i)));
            } else {
                accept = false;
                reportError(line, column, "Parameter '" + paramNames.get(i) + "' is declared more than once");
            }
        }
        
        MethodDescriptor descriptor = new MethodDescriptor(name, type, paramNames, paramTypes, paramSymbolTable);

        // Check if such method name already exists
        if (!methodSymbolTable.canAdd(name)) {
            accept = false;
            reportError(line, column, "Method '" + name + "' is declared more than once");
        } else {
            methodSymbolTable.add(methodDeclASTNode.getMethodName(), descriptor);
        }
        
        // Add and remove Param/Local Symbol Tables while executing block visitor
        symbolTableStack.push(descriptor.getParamSymbolTable());

        currentMethodName = name;
        accept = methodDeclASTNode.getBlock().accept(this) && accept;
        currentMethodName = null;
        
        symbolTableStack.pop();

        if (type != Enums.Type.VOID && !methodDeclASTNode.getBlock().allPathsReturn()) {
            accept = false;
            reportError(line, column, "Not all control paths return");
        }
        return accept;
    }

    public boolean visit(ReturnASTNode returnASTNode) {
        boolean correct = true;
        ExpressionASTNode expr = returnASTNode.getExpression();
        
        Assertion.check(currentMethodName != null);
        Assertion.check(!methodSymbolTable.canAdd(currentMethodName));

        Enums.Type methodType = methodSymbolTable.get(currentMethodName).getType();
        
        if (expr == null) {
            if (methodType != Enums.Type.VOID) {
                correct = false;
                reportError(returnASTNode.getLine(), returnASTNode.getColumn(), 
                        "Method " + currentMethodName + " must return expression of type '" +
                        Enums.getTypeName(methodType) + "'");
            }
        }
        else {
            if (!expr.accept(this)) {
                correct = false;
            } else {
                if (methodType != expr.getType()) {
                    correct = false;
                    if (expr.getType() != Enums.Type.EXPR) {
                        reportError(returnASTNode.getLine(), returnASTNode.getColumn(), 
                                "Type of returned expression '" +
                                Enums.getTypeName(expr.getType()) + "' does not match enclosing method's return type '" +
                                Enums.getTypeName(methodType) + "'");
                    }
                }
            }
        }
        return correct;
    }

    public boolean visit(SingleFieldDeclASTNode singleFieldDeclASTNode) {
        boolean correct = true;
        Enums.Type type = singleFieldDeclASTNode.getType();
        String name = singleFieldDeclASTNode.getName();
        int length = singleFieldDeclASTNode.getLength();
        int line = singleFieldDeclASTNode.getLine();
        int column = singleFieldDeclASTNode.getColumn();
        if (Enums.isTypeArray(type)) {
            if (length == -1) {
                // Size parsed incorrectly because of overflow
                reportError(line, column, "Array size out of range");
                correct = false;
            } else if (length == 0) {
                reportError(line, column, "Array size can't be 0");
                correct = false;
            }
        }
        if (!symbolTableStack.top().canAdd(name)) {
            correct = false;
            reportError(line, column, "Variable '" + name + "' is already declared");
        } else {
            LocalDescriptor descriptor = new LocalDescriptor(name, type, length);
            symbolTableStack.top().add(name, descriptor);
        }
        return correct;
    }

    public boolean visit(TernaryOpASTNode ternaryOpASTNode) {
        boolean correct = true;
        
        ExpressionASTNode boolExpr = ternaryOpASTNode.getBoolExpression();
        if (!boolExpr.accept(this))
            correct = false;
        
        ExpressionASTNode trueExpr = ternaryOpASTNode.getTrueExpression();
        if (!trueExpr.accept(this))
            correct = false;
        
        ExpressionASTNode falseExpr = ternaryOpASTNode.getFalseExpression();
        if (!falseExpr.accept(this))
            correct = false;
        
        if (boolExpr.getType() != Enums.Type.BOOL_SINGLE) {
            correct = false;
            reportError(ternaryOpASTNode.getLine(), ternaryOpASTNode.getColumn(),
                    "First expression of the ternary must be boolean");
        }
        
        if (trueExpr.getType() != falseExpr.getType()) {
            correct = false;
            reportError(ternaryOpASTNode.getLine(), ternaryOpASTNode.getColumn(),
                    "2 possible outcomes of the ternary must have the same type");
        }
        else ternaryOpASTNode.setType(trueExpr.getType());
        
        return correct;
    }

    public boolean visit(UnaryMinusASTNode unaryMinusASTNode) {
        boolean correct = true;
        ExpressionASTNode expr = unaryMinusASTNode.getExpression();
        
        if (!expr.accept(this))
            correct = false;
        
        if (expr.getType() != Enums.Type.INT_SINGLE) {
            correct = false;
            reportError(expr.getLine(), expr.getColumn(),
                    "You can only negate expression having type of int");
        }
        else unaryMinusASTNode.setType(Enums.Type.INT_SINGLE);
        
        return correct;
    }

    public boolean visit(UnaryNotASTNode unaryNotASTNode) {
        boolean correct = true;
        ExpressionASTNode expr = unaryNotASTNode.getExpression();
        
        if (!expr.accept(this))
            correct = false;
        
        if (expr.getType() != Enums.Type.BOOL_SINGLE) {
            correct = false;
            reportError(expr.getLine(), expr.getColumn(),
                    "You can perform 'not' only on boolean expression");
        }
        else unaryNotASTNode.setType(Enums.Type.BOOL_SINGLE);
        
        return correct;
    }

    public boolean visit(WhileASTNode whileASTNode) {
        boolean correct = true;
        ExpressionASTNode expr = whileASTNode.getCondition();
        
        correct = expr.accept(this) && correct;
        
        if (expr.getType() != Enums.Type.BOOL_SINGLE) {
            correct = false;
            reportError(whileASTNode.getLine(), whileASTNode.getColumn(), 
                    "expression inside the while loop must be boolean type");
        }
        InsideLoopsNumber++;
        if (!whileASTNode.getBlock().accept(this))
            correct = false;
        InsideLoopsNumber--;
        
        return correct;
    }

    public GlobalSymbolTable getGlobalsSymbolTable() {
        return globalSymbolTable;
    }
}
