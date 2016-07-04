package edu.mit.compilers.utils;

public class Address {

    private static String getStackAddress(String base, int initialOffset, String index) {
        if (index.equals("0")) {
            return "" + initialOffset + "(" + base + ")";
        } else {
            return "" + initialOffset + "(" + base + "," + index + ",8)";
        }
    }
    public static String getGlobalArray(String label, String index) {
        return label + "(," + index + ",8)";
    }

    public static String getGlobal(String label) {
        return label + "(%rip)";
    }

    public static String getLocalArray(int initialOffset, String index) {
        return initialOffset + "(%rbp," + index + ",8)";
    }

    public static String getLocal(int offset) {
        return offset + "(%rbp)";
    }

    private static String[] paramRegs = {"%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9"};

    public static String getParam(int index) {
        return paramRegs[index];
    }

    public static String getParam(int index, boolean isCaller) {
        if (index < 6) {
            return paramRegs[index];
        } else {
            // isCaller == true : 7 = 0(%rsp), 8 = 8(%rsp)
            // isCaller == false: 7 = 16(%rbp), 8 = 24(%rbp)
            if (isCaller) {
                return getStackAddress("%rsp", (index-6)*8, "0");
            } else {
                return getStackAddress("%rbp", (index-4)*8, "0");
            }
        }
    }
    public static String getRelativeSP(int offset) {
        return offset + "(%rsp)";
    }
}
