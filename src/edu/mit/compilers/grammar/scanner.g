header {
package edu.mit.compilers.grammar;
}

options
{
  mangleLiteralPrefix = "TK_";
  language = "Java";
}

{@SuppressWarnings("unchecked")}
class DecafScanner extends Lexer;
options
{
  k = 2;
}

tokens 
{
    "boolean";
    "break";
    "callout";
    "continue";
    "else";
    "false";
    "for";
    "while";
    "if";
    "int";
    "return";
    "true";
    "void";
}

// Selectively turns on debug tracing mode.
// You can insert arbitrary Java code into your parser/lexer this way.
{
  /** Whether to display debug information. */
  private boolean trace = false;

  public void setTrace(boolean shouldTrace) {
    trace = shouldTrace;
  }
  @Override
  public void traceIn(String rname) throws CharStreamException {
    if (trace) {
      super.traceIn(rname);
    }
  }
  @Override
  public void traceOut(String rname) throws CharStreamException {
    if (trace) {
      super.traceOut(rname);
    }
  }
}

//Note that here, the {} syntax allows you to literally command the lexer
//to skip mark this token as skipped, or to advance to the next line
//by directly adding Java commands.
WS_ : (' ' | '\t' | '\n' {newline();}) {_ttype = Token.SKIP; };
SL_COMMENT : "//" (~'\n')* '\n' {_ttype = Token.SKIP; newline (); };

// Random useful characters

LCURLY options { paraphrase = "}"; } :
    "{";
RCURLY options { paraphrase = "}"; } :
    "}";

LROUND options { paraphrase = "("; } :
    "(";
RROUND options { paraphrase = ")"; } :
    ")";

LSQUARE options {paraphrase = "["; } :
    "[";
RSQUARE options {paraphrase = "]"; } :
    "]";

COMMA   options {paraphrase = ","; } :
    ",";
QMARK   options {paraphrase = "?"; } :
    "?";
COLON   options {paraphrase = ":"; } :
    ":";
SEMICLN options {paraphrase = ";"; } :
    ";";

ARRAY_LEN   : "@";

ID options { paraphrase = "an identifier"; } :
    ALPHA (ALPHA_NUM)*;

// Literals
INT_LITERAL options {paraphrase = "integer literal"; } :
    DEC_LITERAL | HEX_LITERAL;

CHAR_LITERAL: "'" CHAR "'";
STR_LITERAL : '"' (CHAR)* '"'; 

// Operations
MINUS       : {LA(2) != '='}? "-";
PLUS        : {LA(2) != '='}? "+";

MUL_OP      : {LA(2) != '='}? ("*" | "/" | "%");

ASSIGN_OP  	: {LA(2) != '='}? "=";

MODIFY_OP   : {LA(2) == '='}? ("+=" | "-=");

REL_OP     	: {LA(2) != '='}? ("<"  | ">") |
              {LA(2) == '='}? ("<=" | ">=");

EQ_OP       : {LA(2) == '='}? ("==" | "!=");

LOG_AND    	: "&&";
LOG_OR      : "||";
LOG_NOT     : {LA(2) != '='}? "!";

/////////////////////////////////////////////
// Protected stuff

protected ESC :
    '\\' ('"' | '\'' | '\\' | 'n' | 't');

protected CHAR:
    ESC |
    ' '..'!' | // skip "
    '#'..'&' | // skip '
    '('..'[' | // skip \
    ']'..'~'
    ;

protected DIGIT :
    '0'..'9';
protected HEX_DIGIT :
    DIGIT | 'a'..'f' | 'A'..'F';

protected ALPHA :
    'a'..'z' | 'A'..'Z' | '_';

protected ALPHA_NUM :
    ALPHA | DIGIT;

protected DEC_LITERAL :
    {LA(2) != 'x'}? (DIGIT)+; // not a 0x number
protected HEX_LITERAL :
    "0x" (HEX_DIGIT)+;
