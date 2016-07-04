package edu.mit.compilers.utils;

import edu.mit.compilers.utils.Enums.Type;

public class Consts {

    public final static int REG_SIZE = 8;
    public final static String ARRAY_OUT_OF_BOUNDS = "Array";
    
    public static int allignSize(Type type) {
        switch (type) {
        case INT_SINGLE:
        case INT_ARRAY:
        case BOOL_SINGLE:
        case BOOL_ARRAY:
            return 8;
        default:
            throw new IllegalArgumentException("Illegal type: " + Enums.getTypeName(type));
        }
    }
    
    public static int getUnitSize(Type type) {
        switch (type) {
        case INT_SINGLE:
        case INT_ARRAY:
            return 8;
        case BOOL_SINGLE:
        case BOOL_ARRAY:
            return 8;
        default:
            throw new IllegalArgumentException("Illegal type: " + Enums.getTypeName(type));
        }
    }
}
