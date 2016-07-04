package edu.mit.compilers;

import edu.mit.compilers.astnode.*;
import edu.mit.compilers.instruction.*;
import edu.mit.compilers.utils.Address;
import edu.mit.compilers.utils.Consts;
import edu.mit.compilers.utils.Enums;
import edu.mit.compilers.utils.StateAccumulator;

import java.io.PrintStream;
import java.util.*;

public class AssemblerVisitor {

    private int visitCount = 0;
    
    private int ifCounter = 0;
    private int forCounter = 0;
    private int whileCounter = 0;
    private int locationCounter = 0;
    private int shortCircuitCounter = 0;
    
    private Stack<String> loopStarts = new Stack<String>();
    private Stack<String> loopEnds = new Stack<String>();
    
    private StateAccumulator accumulator;
    // Symbol Tables
    private SymbolTable<MethodDescriptor> methodSymbolTable;
    private SymbolTable<CalloutDescriptor> calloutSymbolTable;
    private GlobalSymbolTable globalSymbolTable;
    private SymbolTableStack symbolTableStack;
    private String currentMethodName;
    
    private SymbolTableStack lastEnclosingSymbolTableStack;

    LinkedList<Instruction> instructions;

    private Map<String, String> tempRegLoc = new HashMap<String, String>();
    
    private String assignRegister = "%rax";

    public AssemblerVisitor(PrintStream out, StateAccumulator accumulator) {
        this.accumulator = accumulator;
        Set<String> globalSet = new HashSet<>();
        methodSymbolTable = new SymbolTable<>(globalSet);
        calloutSymbolTable = new SymbolTable<>(globalSet);
        globalSymbolTable = new GlobalSymbolTable(globalSet);
        symbolTableStack = new SymbolTableStack();
        symbolTableStack.push(globalSymbolTable);
        lastEnclosingSymbolTableStack = new SymbolTableStack();
        instructions = new LinkedList<Instruction>();
    }

    public void visit(ProgramASTNode programASTNode) {
        // TODO construct Program-specific instructions and labels
        for (CalloutDeclASTNode node : programASTNode.getCallouts()) {
            instructions.addAll(node.accept(this));
        }
        instructions.add(new PlainTextInstr("######## GLOBAL VARIABLES ########"));
        instructions.add(new PlainTextInstr(".bss"));
        for (FieldDeclASTNode node : programASTNode.getFields()) {
            instructions.addAll(node.accept(this));
        }
        instructions.add(new PlainTextInstr("############## CODE ##############"));
        instructions.add(new PlainTextInstr(".text"));
        instructions.add(new PlainTextInstr(".globl main"));
        for (MethodDeclASTNode node : programASTNode.getMethods()) {
            instructions.addAll(node.accept(this));
        }
        for (Map.Entry<String, String> entry : accumulator.getStrliteralToLabel().entrySet()) {
            instructions.add(new StringLiteralInstr(entry.getValue(), entry.getKey()));
        }
    }

    public LinkedList<Instruction> visit(ArithmeticOpASTNode arithmeticOpASTNode) {
        // TODO(hayk): test
        // TODO(all): review
        LinkedList<Instruction> result = new LinkedList<>();
        int count = visitCount++;
        result.add(new PlainTextInstr("########\tSTART\t" + count + " " + Enums.getOpName(arithmeticOpASTNode.getOp())));
        
        result.addAll(getOperandValues(arithmeticOpASTNode.leftOperand(), arithmeticOpASTNode.rightOperand()));
        // Left operand in %rax, right operand in %r10
        // So need %rax = %rax OP %r10
        switch (arithmeticOpASTNode.getOp()) {
        case PLUS:
//            result.add(new AddInstr("%r10", "%rax"));
            result.add(new AddInstr("%r10", assignRegister));
            break;
        case MINUS:
//            result.add(new SubInstr("%r10", "%rax"));
            result.add(new SubInstr("%r10", assignRegister));
            break;
        case MUL:
//            result.add(new MulInstr("%r10", "%rax"));
            result.add(new MulInstr("%r10", assignRegister));
            break;
        case DIV:
            if (assignRegister.equals("%rax")) {
                result.add(new DivInstr("%r10"));
            } else {
                result.add(new MoveInstr(assignRegister, "%rax"));
                result.add(new DivInstr("%r10"));
                result.add(new MoveInstr("%rax", assignRegister));
            }
            break;
        case MOD:
            if (assignRegister.equals("%rax")) {
                result.add(new DivInstr("%r10"));
                result.add(new MoveInstr("%rdx", "%rax"));
            } else {
                result.add(new MoveInstr(assignRegister, "%rax"));
                result.add(new DivInstr("%r10"));
                result.add(new MoveInstr("%rdx", assignRegister));
            }
            break;
        default:
            throw new RuntimeException("Not implemented for operation " + Enums.getOpName(arithmeticOpASTNode.getOp()));
        }
        
        result.add(new PlainTextInstr("########\tEND\t" + count + " " + Enums.getOpName(arithmeticOpASTNode.getOp())));
        return result;
    }
    
    public LinkedList<Instruction> visit(LogicalOpASTNode logicalOpASTNode) {
        // TODO(hayk): test
        // TODO(all): review
        LinkedList<Instruction> result = new LinkedList<>();
        int count = visitCount++;
        shortCircuitCounter++;
        String shortLabel = "_short_circuit_" + shortCircuitCounter;
        result.add(new PlainTextInstr("########\tSTART\t" + count + " " + Enums.getOpName(logicalOpASTNode.getOp())));
        result.addAll(logicalOpASTNode.leftOperand().accept(this));
//        result.add(new CmpInstr("$1", "%rax"));
        result.add(new CmpInstr("$1", assignRegister));
        switch (logicalOpASTNode.getOp()) {
            case AND:
                result.add(new JumpInstr(shortLabel, Enums.RelationalOp.NEQ));
//                result.add(new PushInstr("%rax"));
                result.add(new PushInstr(assignRegister));
                result.addAll(logicalOpASTNode.rightOperand().accept(this));
                result.add(new PopInstr("%r10"));
//                result.add(new AndInstr("%r10", "%rax"));
                result.add(new AndInstr("%r10", assignRegister));
                break;
            case OR:
                result.add(new JumpInstr(shortLabel, Enums.RelationalOp.EQ));
//                result.add(new PushInstr("%rax"));
                result.add(new PushInstr(assignRegister));
                result.addAll(logicalOpASTNode.rightOperand().accept(this));
                result.add(new PopInstr("%r10"));
//                result.add(new OrInstr("%r10", "%rax"));
                result.add(new OrInstr("%r10", assignRegister));
                break;
            default:
                throw new RuntimeException("Not implemented for operation " + Enums.getOpName(logicalOpASTNode.getOp()));
        }
        result.add(new LabelInstr(shortLabel));
        // result.addAll(getOperandValues(logicalOpASTNode.leftOperand(), logicalOpASTNode.rightOperand()));
        // Left operand in %rax, right operand in %r10
        // So need %rax = %rax OP %r10
        
        switch (logicalOpASTNode.getOp()) {
        case AND:
//            result.add(new AndInstr("%r10", "%rax"));
            result.add(new AndInstr("%r10", assignRegister));
            break;
        case OR:
//            result.add(new OrInstr("%r10", "%rax"));
            result.add(new OrInstr("%r10", assignRegister));
            break;
        default:
            throw new RuntimeException("Not implemented for operation " + Enums.getOpName(logicalOpASTNode.getOp()));
        }
        result.add(new PlainTextInstr("########\tEND\t" + count + " " + Enums.getOpName(logicalOpASTNode.getOp())));
        return result;
    }
    
    public LinkedList<Instruction> visit(RelationalOpASTNode relationalOpASTNode) {
        LinkedList<Instruction> result = new LinkedList<>();
        int count = visitCount++;
        result.add(new PlainTextInstr("########\tSTART\t" + count + " " + Enums.getOpName(relationalOpASTNode.getOp())));
        
        result.addAll(getOperandValues(relationalOpASTNode.leftOperand(), relationalOpASTNode.rightOperand()));
//        result.add(new CmpInstr("%r10", "%rax"));
        result.add(new CmpInstr("%r10", assignRegister));
        result.add(new MoveInstr("$0", "%rax"));        // TODO: Can be optimized by switching bools to 1 byte. 
        result.add(new SetInstr(relationalOpASTNode.getOp(), "%al"));
        
        if (!assignRegister.equals("%rax")) {
            result.add(new MoveInstr("%rax", assignRegister));
        }
        
        result.add(new PlainTextInstr("########\tEND\t" + count + " " + Enums.getOpName(relationalOpASTNode.getOp())));
        return result;
    }
    
    private LinkedList<Instruction> getOperandValues(ExpressionASTNode left, ExpressionASTNode right) {
        // TODO(hayk): test
        LinkedList<Instruction> result = new LinkedList<>();
        result.addAll(right.accept(this));                  // Result of right operand returned in %rax
//        result.add(new PushInstr(null, "%rax"));            // Save the value of right operand in the stack
        result.add(new PushInstr(null, assignRegister));
        result.addAll(left.accept(this));                   // Result of left operand returned in %rax
        result.add(new PopInstr(null, "%r10"));             // Keep the value of right operand in %r10
        return result;
    }

    public LinkedList<Instruction> visit(ArrayLenASTNode arrayLenASTNode) {
        LinkedList<Instruction> result = new LinkedList<>();
        int length = symbolTableStack.getDescriptor(arrayLenASTNode.getName()).getLength();
//        result.add(new MoveInstr("$" + length, "%rax"));
        result.add(new MoveInstr("$" + length, assignRegister));
        return result;
    }

    public LinkedList<Instruction> visit(AssignStmtASTNode assignStmtASTNode) {
        // TODO(hayk): test
        LinkedList<Instruction> result = new LinkedList<>();
        if (assignStmtASTNode.isSkip()) {
            return result;
        }
        int count = visitCount++;
        result.add(new PlainTextInstr("########\tSTART\t" + count + " " + assignStmtASTNode.getAssignment().toString()));
       
        LocationASTNode location = assignStmtASTNode.getLocation();
        String locationAddress;
        
        switch (location.getLocationType()) {
        case ARRAY:
            result.addAll(assignStmtASTNode.getExpression().accept(this));
            
            result.add(new PushInstr(null, "%rax")); // save rvalue in stack
            
            result.addAll(location.getIndex().accept(this)); // array index is in %rax
            result.addAll(checkArrayBounds(location));
            
            locationAddress = symbolTableStack.getDescriptor(location.getName()).getAddress("%rax");
            
            result.add(new PopInstr("_arr_ok_" + locationCounter, "%r10")); // save the result of rvalue in %r10
            locationCounter++;
            break;
        case SINGLE:
            locationAddress = getOptimizedLocationAddress(location.getName());
            
            if (locationAddress.length() <= 4 && // is register 
                    assignStmtASTNode.getAssignment() == Enums.Assignment.EQUALS) {
                assignRegister = locationAddress;
                result.addAll(assignStmtASTNode.getExpression().accept(this));
                assignRegister = "%rax";
                return result;
            }
            result.addAll(assignStmtASTNode.getExpression().accept(this));

//            locationAddress = symbolTableStack.getDescriptor(location.getName()).getAddress();
//            result.add(new MoveInstr("%rax", "%r10"));
            
            switch (assignStmtASTNode.getAssignment()) {
            case EQUALS:
                result.add(new MoveInstr("%rax", locationAddress));
                break;
            case PLUS_EQ:
                result.add(new AddInstr("%rax", locationAddress));
                break;
            case MINUS_EQ:
                result.add(new SubInstr("%rax", locationAddress));
                break;
            default:
                throw new RuntimeException("Not implemented for assignmend " + assignStmtASTNode.getAssignment());
            }
            return result;
        default:
            throw new RuntimeException("Not implemented for " + location.getLocationType());
        }

        switch (assignStmtASTNode.getAssignment()) {
        case EQUALS:
            result.add(new MoveInstr("%r10", locationAddress));
            break;
        case PLUS_EQ:
            result.add(new AddInstr("%r10", locationAddress));
            break;
        case MINUS_EQ:
            result.add(new SubInstr("%r10", locationAddress));
            break;
        default:
            throw new RuntimeException("Not implemented for assignmend " + assignStmtASTNode.getAssignment());
        }
        result.add(new PlainTextInstr("########\tEND\t" + count + " " + assignStmtASTNode.getAssignment().toString()));
        return result;
    }

    public LinkedList<Instruction> visit(BlockASTNode blockASTNode) {
        LinkedList<Instruction> result = new LinkedList<>();
        int count = visitCount++;
        result.add(new PlainTextInstr("########\tSTART\t" + count + " BLOCK"));
        
        LocalSymbolTable localSymbolTable = new LocalSymbolTable(symbolTableStack.top().getStackOffset());
        
        symbolTableStack.push(localSymbolTable);
        for (FieldDeclASTNode field : blockASTNode.getFields()) {
            result.addAll(field.accept(this));
        }
        for (ASTNode stmt : blockASTNode.getStatements()) {
            result.addAll(stmt.accept(this));
        }
        
        symbolTableStack.pop();
        
        // Move %rsp back to the location before the block.
        result.add(new LeaInstr(null, Address.getLocal(symbolTableStack.top().getStackOffset()), "%rsp"));
        result.add(new PlainTextInstr("########\tEND\t" + count + " BLOCK"));
        return result;
    }

    public LinkedList<Instruction> visit(BoolLiteralASTNode boolLiteralASTNode) {
        LinkedList<Instruction> result = new LinkedList<>();
        if (boolLiteralASTNode.getValue()) {
//            result.add(new MoveInstr("$1", "%rax"));
            result.add(new MoveInstr("$1", assignRegister));
        } else {
//            result.add(new MoveInstr("$0", "%rax"));
            result.add(new MoveInstr("$0", assignRegister));
        }
        return result;
    }

    public LinkedList<Instruction> visit(BreakASTNode breakASTNode) {
        LinkedList<Instruction> result = new LinkedList<Instruction>();
        int count = visitCount + 1;
        
        result.add(new PlainTextInstr("########\tSTART\t" + count + " break"));
        result.add(new JumpInstr(loopEnds.peek(), null));
        result.add(new PlainTextInstr("########\tEND\t" + count + " break"));
        
        return result;
    }

    public LinkedList<Instruction> visit(CalloutDeclASTNode calloutDeclASTNode) {
        LinkedList<Instruction> result = new LinkedList<Instruction>();
        String name = calloutDeclASTNode.getName();
        calloutSymbolTable.add(name, new CalloutDescriptor(name));
        return result;
    }

    public LinkedList<Instruction> visit(ContinueASTNode continueASTNode) {
        LinkedList<Instruction> result = new LinkedList<Instruction>();
        int count = visitCount + 1;
        
        result.add(new PlainTextInstr("########\tSTART\t" + count + " continue"));
        
        FieldSymbolTable<LocalDescriptor> cycleSymbolTable = lastEnclosingSymbolTableStack.top();
        result.add(new LeaInstr(null, Address.getLocal(cycleSymbolTable.getStackOffset()), "%rsp"));
        
        result.add(new JumpInstr(loopStarts.peek(), null));
        result.add(new PlainTextInstr("########\tEND\t" + count + " continue"));
        return result;
    }

    public LinkedList<Instruction> visit(ExpressionASTNode expressionASTNode) {
        LinkedList<Instruction> result = new LinkedList<>();
//        int count = visitCount++;
//        result.add(new PlainTextInstr("########\tSTART\t" + count + " EXPRESSION"));
        result.addAll(expressionASTNode.getExpression().accept(this));
        
//        result.add(new PlainTextInstr("########\tEND\t" + count + " EXPRESSION"));
        return result;
    }

    public LinkedList<Instruction> visit(FieldDeclASTNode fieldDeclASTNode) {
        LinkedList<Instruction> result = new LinkedList<Instruction>();
        for (SingleFieldDeclASTNode node : fieldDeclASTNode.getSingleFieldDecls()) {
            result.addAll(node.accept(this));
        }
        return result;
    }

    public LinkedList<Instruction> visit(ForASTNode forASTNode) {
        LinkedList<Instruction> result = new LinkedList<Instruction>();
        
        int count = visitCount;
        String beginFor = "_begin_for_" + forCounter;
        loopStarts.push(beginFor);
        String endFor   = "_end_for_" + forCounter++;
        loopEnds.push(endFor);

        String index = getOptimizedLocationAddress(forASTNode.getLocation().getName());

        long step;
        if (forASTNode.getIncrementStep() == null) {
            step = 1;
        } else {
            step = forASTNode.getIncrementStep().getValue();
        }
        
        result.add(new PlainTextInstr("########\tSTART\t" + count + " FOR"));
        
        // calculate and push initial and final values.
        result.addAll(forASTNode.getInitialValue().accept(this));
        result.add(new MoveInstr("%rax", index));                   // Initial value of FOR in index
        
        result.addAll(forASTNode.getFinalValue().accept(this));
        
        result.add(new SubInstr("$" + Consts.REG_SIZE, "%rsp"));    // Move stack pointer to make space for final value
        result.add(new MoveInstr("%rax", "(%rsp)"));
        symbolTableStack.top().push(Consts.REG_SIZE);               // Notify the symbolstack that %rsp changed

        result.add(new SubInstr("$" + step, index));        // optimization: if there is no break/continue don't do add/sub step from index
        result.add(new LabelInstr(beginFor));
        result.add(new AddInstr("$" + step, index));        // increment index        
        
        result.add(new MoveInstr(index, "%r10"));
        result.add(new CmpInstr("(%rsp)", "%r10"));
        result.add(new JumpInstr(endFor, Enums.RelationalOp.GEQ));
        
        lastEnclosingSymbolTableStack.push(symbolTableStack.top());
        result.addAll(forASTNode.getBlock().accept(this));
        lastEnclosingSymbolTableStack.pop();

        result.add(new JumpInstr(beginFor));                    // jump to the beginning
        result.add(new LabelInstr(endFor));                     // end of for
        
        result.add(new AddInstr("$" + Consts.REG_SIZE, "%rsp"));  // Pop final values from the stack by moving the stack pointer
        symbolTableStack.top().pop(Consts.REG_SIZE);

        result.add(new PlainTextInstr("########\tEND\t" + count + " FOR"));
        
        loopStarts.pop();
        loopEnds.pop();
        
        return result;
    }

    public LinkedList<Instruction> visit(IfASTNode ifASTNode) {
        LinkedList<Instruction> result = new LinkedList<Instruction>();
        
        int count = visitCount;
        int ifCnt = ifCounter++;
        result.add(new PlainTextInstr("########\tSTART\t" + count + " IF"));
        
        result.addAll(ifASTNode.getCondition().accept(this));
        result.add(new CmpInstr("$0", "%rax"));
        
        if (ifASTNode.hasElse()) {
            result.add(new JumpInstr("_else_" + ifCnt, Enums.RelationalOp.EQ));
        } else {
            result.add(new JumpInstr("_end_if_" + ifCnt, Enums.RelationalOp.EQ));
        }
        
        result.addAll(ifASTNode.getBlockTrue().accept(this));
        if (ifASTNode.hasElse()) {
            result.add(new JumpInstr("_end_if_" + ifCnt, null));
            result.add(new LabelInstr("_else_" + ifCnt));
            result.addAll(ifASTNode.getBlockFalse().accept(this));
        }
        result.add(new LabelInstr("_end_if_" + ifCnt));
        
        result.add(new PlainTextInstr("########\tEND\t" + count + " IF"));
        return result;
    }

    public LinkedList<Instruction> visit(IntLiteralASTNode intLiteralASTNode) {
        LinkedList<Instruction> result = new LinkedList<Instruction>();
//        result.add(new MoveInstr("$"+intLiteralASTNode.getValue(), "%rax"));
        result.add(new MoveInstr("$"+intLiteralASTNode.getValue(), assignRegister));
        return result;
    }

    public LinkedList<Instruction> visit(LocationASTNode locationASTNode) {
        LinkedList<Instruction> result = new LinkedList<Instruction>();
        
        int count = visitCount;
        result.add(new PlainTextInstr("########\tSTART\t" + count + " LOCATION"));
        
        switch (locationASTNode.getLocationType()) {
        case ARRAY:
            
            result.addAll(locationASTNode.getIndex().accept(this)); // index in %rax
            result.addAll(checkArrayBounds(locationASTNode));
            
//            result.add(new MoveInstr("_arr_ok_" + locationCounter, symbolTableStack.getDescriptor(locationASTNode.getName()).getAddress("%rax"), "%rax"));
            result.add(new MoveInstr("_arr_ok_" + locationCounter, symbolTableStack.getDescriptor(locationASTNode.getName()).getAddress(assignRegister), assignRegister));
            locationCounter++;
            break;
        case SINGLE:
            String register = getOptimizedLocationAddress(locationASTNode.getName());
            result.add(new PlainTextInstr("### LOCATION " + locationASTNode.getName()));
            result.add(new PlainTextInstr("### " + register));
//            result.add(new MoveInstr(register, "%rax"));
            if (!register.equals(assignRegister)) {
                result.add(new MoveInstr(register, assignRegister));
            }
            break;
        default:
            throw new RuntimeException("Not implemented for type" + locationASTNode.getType());
        }
        
        result.add(new PlainTextInstr("########\tEND\t" + count + " LOCATION"));
        return result;
    }

    public String getOptimizedLocationAddress(String locName) {
        String register = accumulator.varToReg.get(symbolTableStack.getDescriptor(locName).getVariableId());
        if (register == null) {
            register = symbolTableStack.getDescriptor(locName).getAddress();
        } else if (tempRegLoc.containsKey(register)) {
            register = tempRegLoc.get(register);
        }
        return register;
    }

    private List<Instruction> checkArrayBounds(LocationASTNode locationASTNode) {
        // assuming index is in %rax
        LinkedList<Instruction> result = new LinkedList<Instruction>();
//        result.add(new CmpInstr("$" + symbolTableStack.getDescriptor(locationASTNode.getName()).getLength(), "%rax"));
        result.add(new CmpInstr("$" + symbolTableStack.getDescriptor(locationASTNode.getName()).getLength(), assignRegister));
        result.add(new JumpInstr("_arr_err_" + locationCounter, Enums.RelationalOp.GEQ));
//        result.add(new CmpInstr("$0", "%rax"));
        result.add(new CmpInstr("$0", assignRegister));
        result.add(new JumpInstr("_arr_ok_" + locationCounter, Enums.RelationalOp.GEQ));
        
        String err = "\"" + locationASTNode.getLine() + ":" + locationASTNode.getColumn() + "\tArray '" + locationASTNode.getName() + "' out of bounds\\n\"";
        
        accumulator.addStrliteral(err);
        
        result.addAll(exitInstructions("_arr_err_" + locationCounter, err, -1));
        return result;
    }

    private List<Instruction> exitInstructions(String errLabel, String err, int code) {
        LinkedList<Instruction> result = new LinkedList<Instruction>();
        result.add(new MoveInstr(errLabel, "$" + accumulator.getStrliteralToLabel().get(err), "%rdi"));

        result.add(new MoveInstr("$0", "%rax"));
        result.add(new CallInstr("printf"));
        result.add(new MoveInstr("$60", "%rax"));
        result.add(new MoveInstr("$" + code, "%rdi"));
        result.add(new PlainTextInstr("syscall"));
        
        return result;
    }

    public LinkedList<Instruction> visit(MethodCallASTNode methodCallASTNode) {
        LinkedList<Instruction> result = new LinkedList<>();
        int count = visitCount++;
        result.add(new PlainTextInstr("########\tSTART\t" + count + " METHOD " + methodCallASTNode.getMethodName()));
        
        String name = methodCallASTNode.getMethodName();
        List<MethodCallParam> params = methodCallASTNode.getParams();
        // Save registers in stack
        result.add(new SubInstr("$" + 8*Math.max(params.size(), 6), "%rsp"));
        for(int i = 0; i < 6; i++) {
            String reg = Address.getParam(i, true);
            String stk = ""+(Math.max(0,params.size()-6)*8+8*i)+"(%rsp)";
            result.add(new MoveInstr(reg, stk));
            tempRegLoc.put(reg, stk);
        }
        if (calloutSymbolTable.exists(name)) {
            // Ready the registers & the stack for callout
            for(int i = 0; i < params.size(); i++) {
                MethodCallParam param = params.get(i);
                if (param.getType() == Enums.Type.STR) {
                    result.add(new MoveInstr(
                            "$"+accumulator.getStrliteralToLabel().get(param.getLiteral()),
                            Address.getParam(i, true)
                    ));
                } else {
                    result.addAll(param.getExpr().accept(this));
                    result.add(new MoveInstr("%rax", Address.getParam(i, true)));
                }
            }
            result.add(new MoveInstr("$0", "%rax"));
            result.add(new CallInstr(name));
        } else {
            result.add(new SubInstr("$" + 8*params.size(), "%rsp"));
            for (int i=0;i<params.size();i++) {
                result.addAll(params.get(i).getExpr().accept(this));
//                result.add(new MoveInstr("%rax", Address.getRelativeSP(8*i)));
                String paramName = methodSymbolTable.get(name).getParameterNames().get(i);
                String variableID = methodSymbolTable.get(name).getParamSymbolTable().get(paramName).getVariableId();
                String register = accumulator.varToReg.get(variableID);
                if (register == null) {
                    register = Address.getRelativeSP(8*i);
                }
                result.add(new MoveInstr(assignRegister, register));
            }
            result.add(new MoveInstr("$0", "%rax"));
            result.add(new CallInstr(name));
            result.add(new AddInstr("$" + 8*params.size(), "%rsp"));
        }
        // Restore registers from stack
        for(int i = 0; i < 6; i++) {
            result.add(new MoveInstr(
                    ""+(Math.max(0,params.size()-6)*8+8*i)+"(%rsp)",
                    Address.getParam(i, true)
            ));
        }
        result.add(new AddInstr("$" + 8*Math.max(params.size(), 6), "%rsp"));
        tempRegLoc.clear();

        if (!assignRegister.equals("%rax")) {
            result.add(new MoveInstr("%rax", assignRegister));
        }
            
        result.add(new PlainTextInstr("########\tEND\t" + count + " METHOD " + methodCallASTNode.getMethodName()));
        return result;
    }

    public LinkedList<Instruction> visit(MethodDeclASTNode methodDeclASTNode) {
        LinkedList<Instruction> result = new LinkedList<Instruction>();

        String name = methodDeclASTNode.getMethodName();
        Enums.Type type = methodDeclASTNode.getMethodType();
        List<String> paramNames = methodDeclASTNode.getParameterNames();
        List<Enums.Type> paramTypes = methodDeclASTNode.getParameterTypes();

        FieldSymbolTable<LocalDescriptor> paramSymbolTable = new ParamSymbolTable();
        for (int i = 0; i < paramNames.size(); i++) {
            LocalDescriptor paramDescriptor = new LocalDescriptor(paramNames.get(i), paramTypes.get(i));
            paramSymbolTable.add(paramNames.get(i), paramDescriptor);
        }
        MethodDescriptor descriptor = new MethodDescriptor(name, type, paramNames, paramTypes, paramSymbolTable);
        methodSymbolTable.add(methodDeclASTNode.getMethodName(), descriptor);

        result.add(new EnterInstr(name, 0));

        symbolTableStack.push(descriptor.getParamSymbolTable());
        currentMethodName = name;
        
        result.addAll(methodDeclASTNode.getBlock().accept(this));
        currentMethodName = null;
        symbolTableStack.pop();

        if (methodDeclASTNode.getMethodType() == Enums.Type.VOID) {
            result.add(new MoveInstr("$0", "%rax"));
        }

        result.add(new LeaveInstr("_leave_" + name));
        result.add(new RetInstr());

        return result;
    }

    public LinkedList<Instruction> visit(ReturnASTNode returnASTNode) {
        LinkedList<Instruction> result = new LinkedList<Instruction>();
        int count = visitCount + 1;
        
        result.add(new PlainTextInstr("########\tSTART\t" + count + " RETURN"));
        
        if (returnASTNode.getExpression() != null) {
            result.addAll(returnASTNode.getExpression().accept(this));
        }
        result.add(new JumpInstr("_leave_" + currentMethodName));
        
        result.add(new PlainTextInstr("########\tEND\t" + count + " RETURN"));
        
        return result;
    }

    public LinkedList<Instruction> visit(SingleFieldDeclASTNode singleFieldDeclASTNode) {
        LinkedList<Instruction> result = new LinkedList<Instruction>();
        Enums.Type type = singleFieldDeclASTNode.getType();
        String name = singleFieldDeclASTNode.getName();
        int length = singleFieldDeclASTNode.getLength();

        SymbolTable<LocalDescriptor> symbolTable = symbolTableStack.top();
        LocalDescriptor descriptor = new LocalDescriptor(name, type, length);
        symbolTable.add(name, descriptor);

        if (symbolTable == globalSymbolTable) {
            if (singleFieldDeclASTNode.isArray()) {
                result.add(new GlobalVarInstr(name, type, length));
            } else {
                result.add(new GlobalVarInstr(name, type, 1));
            }
        } else {
            result.add(new SubInstr(null, "$" + String.valueOf(descriptor.getNBytes()), "%rsp"));
            if (descriptor.isArray()) {
                String beginFor = "_begin_for_" + forCounter;
                String endFor   = "_end_for_" + forCounter++;
                // Add for cycle to initialize memory
                result.add(new MoveInstr("$0", "%r10"));
                result.add(new LabelInstr(beginFor));
                result.add(new CmpInstr( "$" + length, "%r10"));
                result.add(new JumpInstr(endFor, Enums.RelationalOp.GEQ));
                result.add(new MoveInstr("$0", descriptor.getAddress("%r10")));
                result.add(new AddInstr("$1", "%r10"));
                result.add(new JumpInstr(beginFor));
                result.add(new LabelInstr(endFor));
            } else {
                String register = accumulator.varToReg.get(descriptor.getVariableId());
                if (register == null) {
                    register = "(%rsp)";
                }
                result.add(new MoveInstr("$0", register));
            }
        }
        return result;
    }

    public LinkedList<Instruction> visit(TernaryOpASTNode ternaryOpASTNode) {
        LinkedList<Instruction> result = new LinkedList<>();
        int count = visitCount;
        result.add(new PlainTextInstr("########\tSTART\t" + count + " Ternary op"));
        
        result.addAll(ternaryOpASTNode.getTrueExpression().accept(this));
//        result.add(new PushInstr("%rax"));
        result.add(new PushInstr(assignRegister));
        
        result.addAll(ternaryOpASTNode.getFalseExpression().accept(this));
//        result.add(new PushInstr("%rax"));
        result.add(new PushInstr(assignRegister));
        
        result.addAll(ternaryOpASTNode.getBoolExpression().accept(this));
        
        result.add(new PopInstr("%r10"));   // false expression
        result.add(new PopInstr("%r11"));   // true expression
//        result.add(new CmpInstr("$0", "%rax"));
        result.add(new CmpInstr("$0", assignRegister));
        
//        result.add(new MoveInstr("%r10", "%rax", Enums.RelationalOp.EQ));
        result.add(new MoveInstr("%r10", assignRegister, Enums.RelationalOp.EQ));
//        result.add(new MoveInstr("%r11", "%rax", Enums.RelationalOp.NEQ));
        result.add(new MoveInstr("%r11", assignRegister, Enums.RelationalOp.NEQ));
        
        result.add(new PlainTextInstr("########\tEND\t" + count + " Ternary op"));
        return result;
    }

    public LinkedList<Instruction> visit(UnaryMinusASTNode unaryMinusASTNode) {
        LinkedList<Instruction> result = new LinkedList<Instruction>();
        int count = visitCount + 1;
        
        result.add(new PlainTextInstr("########\tSTART\t" + count + " minus"));
        
        result.addAll(unaryMinusASTNode.getExpression().accept(this));
//        result.add(new NegInstr(null, "%rax"));
        result.add(new NegInstr(null, assignRegister));
        
        result.add(new PlainTextInstr("########\tEND\t" + count + " minus"));
        
        return result;
    }

    public LinkedList<Instruction> visit(UnaryNotASTNode unaryNotASTNode) {
        LinkedList<Instruction> result = new LinkedList<Instruction>();
        int count = visitCount + 1;
        
        result.add(new PlainTextInstr("########\tSTART\t" + count + " not"));
        
        result.addAll(unaryNotASTNode.getExpression().accept(this));
//        result.add(new XorInstr(null, "$1", "%rax"));
        result.add(new XorInstr(null, "$1", assignRegister));
        
        result.add(new PlainTextInstr("########\tEND\t" + count + " not"));
        
        return result;
    }

    public LinkedList<Instruction> visit(WhileASTNode whileASTNode) {
        LinkedList<Instruction> result = new LinkedList<Instruction>();
        int count = visitCount + 1;
        whileCounter++;
        String startLabel = "while" + Integer.toString(whileCounter);
        loopStarts.push(startLabel);
        String endLabel = "while_done" + whileCounter;
        loopEnds.push(endLabel);
        
        result.add(new PlainTextInstr("########\tSTART\t" + count + " while"));
        result.add(new LabelInstr(startLabel));
        
        result.addAll(whileASTNode.getCondition().accept(this));
        
        result.add(new CmpInstr("$0", "%rax"));
        result.add(new JumpInstr(endLabel, Enums.RelationalOp.EQ));
        
        lastEnclosingSymbolTableStack.push(symbolTableStack.top());
        result.addAll(whileASTNode.getBlock().accept(this));
        lastEnclosingSymbolTableStack.pop();
        
        result.add(new JumpInstr(startLabel, null));
        
        result.add(new LabelInstr(endLabel));
        result .add(new PlainTextInstr("########\tEND\t" + count + " while"));
       
        loopStarts.pop();
        loopEnds.pop();
        
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Instruction instruction : instructions) {
            result.append(instruction.toString());
        }
        return result.toString();
    }
}
