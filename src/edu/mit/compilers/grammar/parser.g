header {
    package edu.mit.compilers.grammar;
}
    
class DecafParser extends Parser;
options {
    importVocab = DecafScanner;
    k = 3;
    buildAST = true;
}

tokens {
    PROGRAM;
    CALLOUT_DECL;
    ARRAY_DECL;
    VAR_DECL;
    FIELD_DECL;
    METHOD_DECL;
    METHOD_CALL;
    BLOCK;
    EXPR;
    ARITH;
    LOCATION;
}

// Java glue code that makes error reporting easier.
// You can insert arbitrary Java code into your parser/lexer this way.
{
    // Do our own reporting of errors so the parser can return a non-zero status
    // if any errors are detected.
    /** Reports if any errors were reported during parse. */
    private boolean error;

    @Override
    public void reportError(RecognitionException ex) {
        // Print the error via some kind of error reporting mechanism.
        error = true;
        reportError(ex.toString());
    }

    @Override
    public void reportError(String s) {
        // Print the error via some kind of error reporting mechanism.
        error = true;
        System.out.println(s);
    }

    public boolean getError() {
        return error;
    }

    // Selectively turns on debug mode.

    /** Whether to display debug information. */
    private boolean trace = false;

    public void setTrace(boolean shouldTrace) {
        trace = shouldTrace;
    }

    @Override
    public void traceIn(String rname) throws TokenStreamException {
        if (trace) {
            super.traceIn(rname);
        }
    }

    @Override
    public void traceOut(String rname) throws TokenStreamException {
        if (trace) {
            super.traceOut(rname);
        }
    }
}

program:
    (callout_decl)* (field_decl)* (method_decl)* EOF!
    {#program = #([PROGRAM, "program"], #program);};

callout_decl:
    TK_callout! ID SEMICLN!
    {#callout_decl = #([CALLOUT_DECL, "callout_decl"],  #callout_decl);};

field_decl:
    type single_field_decl (COMMA! single_field_decl)* SEMICLN!
    {#field_decl = #([FIELD_DECL, "field_decl"],  #field_decl);};

single_field_decl :
    array_decl | ID;

array_decl:
    ID LSQUARE! INT_LITERAL RSQUARE!
    {#array_decl = #([ARRAY_DECL, "array_decl"],  #array_decl);};
//
//var_decl:
//    ID^
//    {#var_decl = #([VAR_DECL, "var_decl"],  #var_decl);};
    
//        protected one_field_decl : ID | ID LSQUARE INT_LITERAL RSQUARE;

type :
    TK_int | TK_boolean;

method_decl :
    (type | TK_void) ID LROUND! (type ID (COMMA! type ID)* )? RROUND! block
    {#method_decl = #([METHOD_DECL, "method_decl"],  #method_decl);};

block:
    LCURLY! (field_decl)* (statement)* RCURLY!
    {#block = #([BLOCK, "block"],  #block);};

statement :
    location (ASSIGN_OP^ | MODIFY_OP^) expr SEMICLN! |
    method_call SEMICLN! |
    TK_if^ LROUND! expr RROUND! block (TK_else! block)? |
    TK_for^ LROUND! ID ASSIGN_OP! expr COMMA! expr (COMMA! INT_LITERAL)? RROUND! block |    
    TK_while^ LROUND! expr RROUND! block |
    TK_return^ (expr)? SEMICLN! |
    TK_break^ SEMICLN! |
    TK_continue^ SEMICLN!;

//assign_op :
//    ASSIGN_OP | MODIFY_OP;

method_call:
    ID LROUND! ((expr | STR_LITERAL) (COMMA! (expr | STR_LITERAL))*)? RROUND!
    {#method_call = #([METHOD_CALL, "method_call"],  #method_call);};

//        protected callout_arg : expr | STR_LITERAL;

        // no need for this rule, the other covers it.
//    ID LROUND (expr (COMMA expr)*)? RROUND |

location :
    (ID | array_location)
    {#location = #([LOCATION, "location"],  #location);};

array_location :
    ID LSQUARE! expr RSQUARE!;

//add_op :
//    ;

literal:
    INT_LITERAL | CHAR_LITERAL | TK_true | TK_false;

//// EXPRESSION

//expr :
//    expr1
//    {#expr = #([EXPR, "expr"],  #expr);};

expr:
    (no_ternary (QMARK^ expr COLON! expr)?)
    {#expr = #([EXPR, "expr"],  #expr);};
    
no_ternary :
    (no_or (LOG_OR^ no_or)*);

no_or :
    no_and (LOG_AND^ no_or)*;

no_and :
    no_eq (EQ_OP^ no_and)*;

no_eq :
    no_ineq (REL_OP^ no_eq)*;

no_ineq :
    no_add ((MINUS | PLUS) no_add)*
    {#no_ineq = #([ARITH, "arith"],  #no_ineq);};

no_add :
    (no_mul (MUL_OP no_mul)*)
    {#no_add = #([ARITH, "arith"],  #no_add);};

no_mul :
    LOG_NOT^ no_mul |
    no_not;

no_not :
    MINUS^ no_not |
    no_minus;

no_minus:
    ARRAY_LEN^ ID |
    LROUND! expr RROUND! |
    literal |
    location |
    method_call;
