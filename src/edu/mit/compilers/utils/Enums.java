package edu.mit.compilers.utils;

import edu.mit.compilers.grammar.DecafParserTokenTypes;

public class Enums {
    public enum Assignment {
        PLUS_EQ, MINUS_EQ, EQUALS
    }

    public enum Location {
        SINGLE, ARRAY
    }

    public enum Type {
        BOOL_SINGLE, INT_SINGLE, BOOL_ARRAY, INT_ARRAY, STR, VOID, EXPR // EXPR means it's undecided!
    }
    
    public enum ArithmeticOp {
        PLUS, MINUS, MUL, DIV, MOD
    };
    
    public enum LogicalOp {
        AND, OR
    };
    
    public enum RelationalOp {
        LEQ, GEQ, LT, GT, EQ, NEQ
    }
    
    public enum FieldType {
        LOCAL, GLOBAL, PARAM;
    }

    public enum ExprToStr {
        SYMBOLIC_VALUE, VARIABLE_ID
    }

    public static Assignment convertStrToAssignment(String opString) {
        if (opString.equals("="))
            return Enums.Assignment.EQUALS;
        else if (opString.equals("+="))
            return Enums.Assignment.PLUS_EQ;
        else if (opString.equals("-="))
            return Enums.Assignment.MINUS_EQ;
        else
            throw new IllegalArgumentException("opString \'" + opString + "\' can not be converted to Enum.Assignment");
    }

    public static Type convertTypeToEnum(int parserType) {
        switch (parserType) {
        case DecafParserTokenTypes.TK_boolean:
            return Type.BOOL_SINGLE;

        case DecafParserTokenTypes.INT_LITERAL:
        case DecafParserTokenTypes.TK_int:
            return Type.INT_SINGLE;

        case DecafParserTokenTypes.STR_LITERAL:
            return Type.STR;

        case DecafParserTokenTypes.EXPR:
            return Type.EXPR;

        case DecafParserTokenTypes.TK_void:
            return Type.VOID;

        default:
            throw new IllegalArgumentException("Can not convert ParserType to Enum.Type, received " + parserType);
        }
    }

    public static Type convertTypeToArray(Type type) {
        switch(type) {
            case INT_SINGLE: return Type.INT_ARRAY;
            case BOOL_SINGLE: return Type.BOOL_ARRAY;
            default: throw new IllegalArgumentException("Can't convert '"+type.name()+"' to an array type");
        }
    }

    public static Type convertTypeToSingle(Type type) {
        switch(type) {
            case INT_ARRAY: return Type.INT_SINGLE;
            case BOOL_ARRAY: return Type.BOOL_SINGLE;
            default: throw new IllegalArgumentException("Can't convert '"+type.name()+"' to a single type");
        }
    }

    public static String getTypeName(Type type) {
        switch (type) {
        case BOOL_SINGLE: return "boolean";
        case INT_SINGLE : return "int";
        case BOOL_ARRAY: return "[boolean]";
        case INT_ARRAY : return "[int]";
        case STR : return "string";
        case VOID: return "void";
        case EXPR: return "unknown";
        default:
            throw new IllegalArgumentException("Can not get the type name of '" + type.name() + "'");
        }
    }

    public static boolean isTypeArray(Type type) {
        return (type == Type.INT_ARRAY || type == Type.BOOL_ARRAY);
    }
        
    public static ArithmeticOp convertArithOpToEnum(String op) {
        switch (op) {
        case "+": return ArithmeticOp.PLUS;
        case "-": return ArithmeticOp.MINUS;
        case "*": return ArithmeticOp.MUL;
        case "/": return ArithmeticOp.DIV;
        case "%": return ArithmeticOp.MOD;
        default:
            throw new IllegalArgumentException("Unrecognized operation '" + op + "'");
        }
    }

    public static String getOpName(ArithmeticOp op) {
        switch (op) {
        case PLUS : return "+";
        case MINUS: return "-";
        case MUL  : return "*";
        case DIV  : return "/";
        case MOD  : return "%";
        default:
            throw new IllegalArgumentException("Illegal Arithmetic Operation Name '" + op.name() + "'");
        }
    }

    public static LogicalOp convertLogOpToEnum(String op) {
        switch (op) {
        case "&&": return LogicalOp.AND;
        case "||": return LogicalOp.OR;
        default:
            throw new IllegalArgumentException("Unrecognized operation '" + op + "'");
        }
    }
    
    public static String getOpName(LogicalOp op) {
        switch (op) {
        case AND: return "&&";
        case OR : return "||";
        default:
            throw new IllegalArgumentException("Illegal Logical Operation Name '" + op.name() + "'");
        }
    }
    
    public static RelationalOp convertRelOpToEnum(String op) {
        switch (op) {
        case "==": return RelationalOp.EQ;
        case "!=": return RelationalOp.NEQ;
        case "<=": return RelationalOp.LEQ;
        case ">=": return RelationalOp.GEQ;
        case "<" : return RelationalOp.LT;
        case ">" : return RelationalOp.GT;
        default:
            throw new IllegalArgumentException("Unrecognized operation '" + op + "'");
        }
    }
    
    public static String getOpName(RelationalOp op) {
        switch (op) {
        case EQ : return "==";
        case NEQ: return "!=";
        case LEQ: return "<=";
        case GEQ: return ">=";
        case LT : return "<";
        case GT : return ">";
        default:
            throw new IllegalArgumentException("Illegal Logical Operation Name '" + op.name() + "'");
        }
    }
}
