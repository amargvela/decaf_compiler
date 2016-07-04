package edu.mit.compilers;

import antlr.CommonAST;
import antlr.Token;

public class CommonASTWithLines extends CommonAST {
    private int line = 0;
    private int column = 0;
    
    private static int currentLine = 0;
    private static int currentColumn = 0;
    
    // This is called for every token
    public CommonASTWithLines() {
        super();
        line = currentLine;
        column = currentColumn;
    }

    // Apparently this method is called only for real tokens
    public void initialize(Token token) { 
        super.initialize(token);
        
        if (token.getLine() > 0) {
            currentLine = token.getLine();
            currentColumn = token.getColumn();
        }
        line = currentLine;
        column = currentColumn;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
