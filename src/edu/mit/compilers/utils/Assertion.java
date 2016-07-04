package edu.mit.compilers.utils;

public class Assertion {
    public static void check(boolean condition) {
        check(condition, "");
    }
    
    public static void check(boolean condition, String error) {
        if (!condition) {
            throw new RuntimeException("Assertion: " + error);
        }
    }
}
