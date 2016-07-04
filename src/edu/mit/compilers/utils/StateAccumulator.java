package edu.mit.compilers.utils;

import edu.mit.compilers.DataflowVisitor;
import edu.mit.compilers.GlobalSymbolTable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Accumulated needed global state while traversing different visitors
 */
public class StateAccumulator {

    private Map<String, String> strliteralToLabel = new HashMap<String, String>();
    private int strliteralCounter = 0;
    public DataflowVisitor dataflowVisitor;
    public GlobalSymbolTable globalSymbolTable;

    public Map<String, String> varToReg = new HashMap<String, String>();

    public void addStrliteral(String literal) {
        if (!strliteralToLabel.containsKey(literal)) {
            strliteralCounter++;
            strliteralToLabel.put(literal, "_str_literal_" + strliteralCounter);
        }
    }

    public Map<String, String> getStrliteralToLabel() {
        return strliteralToLabel;
    }
}
