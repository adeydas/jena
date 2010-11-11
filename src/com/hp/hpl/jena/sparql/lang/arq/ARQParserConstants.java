/* Generated By:JavaCC: Do not edit this line. ARQParserConstants.java */
/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd
 * All rights reserved.
 */
package com.hp.hpl.jena.sparql.lang.arq ;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface ARQParserConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int WS = 6;
  /** RegularExpression Id. */
  int SINGLE_LINE_COMMENT = 7;
  /** RegularExpression Id. */
  int IRIref = 8;
  /** RegularExpression Id. */
  int PNAME_NS = 9;
  /** RegularExpression Id. */
  int PNAME_LN = 10;
  /** RegularExpression Id. */
  int BLANK_NODE_LABEL = 11;
  /** RegularExpression Id. */
  int VAR1 = 12;
  /** RegularExpression Id. */
  int VAR2 = 13;
  /** RegularExpression Id. */
  int LANGTAG = 14;
  /** RegularExpression Id. */
  int A2Z = 15;
  /** RegularExpression Id. */
  int A2ZN = 16;
  /** RegularExpression Id. */
  int KW_A = 17;
  /** RegularExpression Id. */
  int BASE = 18;
  /** RegularExpression Id. */
  int PREFIX = 19;
  /** RegularExpression Id. */
  int SELECT = 20;
  /** RegularExpression Id. */
  int DISTINCT = 21;
  /** RegularExpression Id. */
  int REDUCED = 22;
  /** RegularExpression Id. */
  int DESCRIBE = 23;
  /** RegularExpression Id. */
  int CONSTRUCT = 24;
  /** RegularExpression Id. */
  int ASK = 25;
  /** RegularExpression Id. */
  int LIMIT = 26;
  /** RegularExpression Id. */
  int OFFSET = 27;
  /** RegularExpression Id. */
  int ORDER = 28;
  /** RegularExpression Id. */
  int BY = 29;
  /** RegularExpression Id. */
  int BINDINGS = 30;
  /** RegularExpression Id. */
  int UNDEF = 31;
  /** RegularExpression Id. */
  int ASC = 32;
  /** RegularExpression Id. */
  int DESC = 33;
  /** RegularExpression Id. */
  int NAMED = 34;
  /** RegularExpression Id. */
  int FROM = 35;
  /** RegularExpression Id. */
  int WHERE = 36;
  /** RegularExpression Id. */
  int AND = 37;
  /** RegularExpression Id. */
  int GRAPH = 38;
  /** RegularExpression Id. */
  int OPTIONAL = 39;
  /** RegularExpression Id. */
  int UNION = 40;
  /** RegularExpression Id. */
  int MINUS_P = 41;
  /** RegularExpression Id. */
  int BIND = 42;
  /** RegularExpression Id. */
  int SERVICE = 43;
  /** RegularExpression Id. */
  int LET = 44;
  /** RegularExpression Id. */
  int FETCH = 45;
  /** RegularExpression Id. */
  int EXISTS = 46;
  /** RegularExpression Id. */
  int NOT = 47;
  /** RegularExpression Id. */
  int NOTEXISTS = 48;
  /** RegularExpression Id. */
  int AS = 49;
  /** RegularExpression Id. */
  int GROUP = 50;
  /** RegularExpression Id. */
  int HAVING = 51;
  /** RegularExpression Id. */
  int SEPARATOR = 52;
  /** RegularExpression Id. */
  int AGG = 53;
  /** RegularExpression Id. */
  int COUNT = 54;
  /** RegularExpression Id. */
  int MIN = 55;
  /** RegularExpression Id. */
  int MAX = 56;
  /** RegularExpression Id. */
  int SUM = 57;
  /** RegularExpression Id. */
  int AVG = 58;
  /** RegularExpression Id. */
  int STDDEV = 59;
  /** RegularExpression Id. */
  int SAMPLE = 60;
  /** RegularExpression Id. */
  int GROUP_CONCAT = 61;
  /** RegularExpression Id. */
  int FILTER = 62;
  /** RegularExpression Id. */
  int BOUND = 63;
  /** RegularExpression Id. */
  int COALESCE = 64;
  /** RegularExpression Id. */
  int IN = 65;
  /** RegularExpression Id. */
  int NOT_IN = 66;
  /** RegularExpression Id. */
  int IF = 67;
  /** RegularExpression Id. */
  int BNODE = 68;
  /** RegularExpression Id. */
  int IRI = 69;
  /** RegularExpression Id. */
  int URI = 70;
  /** RegularExpression Id. */
  int CAST = 71;
  /** RegularExpression Id. */
  int CALL = 72;
  /** RegularExpression Id. */
  int STR = 73;
  /** RegularExpression Id. */
  int STRLANG = 74;
  /** RegularExpression Id. */
  int STRDT = 75;
  /** RegularExpression Id. */
  int DTYPE = 76;
  /** RegularExpression Id. */
  int LANG = 77;
  /** RegularExpression Id. */
  int LANGMATCHES = 78;
  /** RegularExpression Id. */
  int IS_URI = 79;
  /** RegularExpression Id. */
  int IS_IRI = 80;
  /** RegularExpression Id. */
  int IS_BLANK = 81;
  /** RegularExpression Id. */
  int IS_LITERAL = 82;
  /** RegularExpression Id. */
  int IS_NUMERIC = 83;
  /** RegularExpression Id. */
  int REGEX = 84;
  /** RegularExpression Id. */
  int SAME_TERM = 85;
  /** RegularExpression Id. */
  int RAND = 86;
  /** RegularExpression Id. */
  int TRUE = 87;
  /** RegularExpression Id. */
  int FALSE = 88;
  /** RegularExpression Id. */
  int DATA = 89;
  /** RegularExpression Id. */
  int INSERT = 90;
  /** RegularExpression Id. */
  int DELETE = 91;
  /** RegularExpression Id. */
  int INSERT_DATA = 92;
  /** RegularExpression Id. */
  int DELETE_DATA = 93;
  /** RegularExpression Id. */
  int DELETE_WHERE = 94;
  /** RegularExpression Id. */
  int MODIFY = 95;
  /** RegularExpression Id. */
  int LOAD = 96;
  /** RegularExpression Id. */
  int CLEAR = 97;
  /** RegularExpression Id. */
  int CREATE = 98;
  /** RegularExpression Id. */
  int ADD = 99;
  /** RegularExpression Id. */
  int MOVE = 100;
  /** RegularExpression Id. */
  int COPY = 101;
  /** RegularExpression Id. */
  int META = 102;
  /** RegularExpression Id. */
  int SILENT = 103;
  /** RegularExpression Id. */
  int DROP = 104;
  /** RegularExpression Id. */
  int INTO = 105;
  /** RegularExpression Id. */
  int TO = 106;
  /** RegularExpression Id. */
  int DFT = 107;
  /** RegularExpression Id. */
  int ALL = 108;
  /** RegularExpression Id. */
  int WITH = 109;
  /** RegularExpression Id. */
  int USING = 110;
  /** RegularExpression Id. */
  int DIGITS = 111;
  /** RegularExpression Id. */
  int INTEGER = 112;
  /** RegularExpression Id. */
  int DECIMAL = 113;
  /** RegularExpression Id. */
  int DOUBLE = 114;
  /** RegularExpression Id. */
  int INTEGER_POSITIVE = 115;
  /** RegularExpression Id. */
  int DECIMAL_POSITIVE = 116;
  /** RegularExpression Id. */
  int DOUBLE_POSITIVE = 117;
  /** RegularExpression Id. */
  int INTEGER_NEGATIVE = 118;
  /** RegularExpression Id. */
  int DECIMAL_NEGATIVE = 119;
  /** RegularExpression Id. */
  int DOUBLE_NEGATIVE = 120;
  /** RegularExpression Id. */
  int EXPONENT = 121;
  /** RegularExpression Id. */
  int QUOTE_3D = 122;
  /** RegularExpression Id. */
  int QUOTE_3S = 123;
  /** RegularExpression Id. */
  int ECHAR = 124;
  /** RegularExpression Id. */
  int STRING_LITERAL1 = 125;
  /** RegularExpression Id. */
  int STRING_LITERAL2 = 126;
  /** RegularExpression Id. */
  int STRING_LITERAL_LONG1 = 127;
  /** RegularExpression Id. */
  int STRING_LITERAL_LONG2 = 128;
  /** RegularExpression Id. */
  int LPAREN = 129;
  /** RegularExpression Id. */
  int RPAREN = 130;
  /** RegularExpression Id. */
  int NIL = 131;
  /** RegularExpression Id. */
  int LBRACE = 132;
  /** RegularExpression Id. */
  int RBRACE = 133;
  /** RegularExpression Id. */
  int LBRACKET = 134;
  /** RegularExpression Id. */
  int RBRACKET = 135;
  /** RegularExpression Id. */
  int ANON = 136;
  /** RegularExpression Id. */
  int SEMICOLON = 137;
  /** RegularExpression Id. */
  int COMMA = 138;
  /** RegularExpression Id. */
  int DOT = 139;
  /** RegularExpression Id. */
  int EQ = 140;
  /** RegularExpression Id. */
  int NE = 141;
  /** RegularExpression Id. */
  int GT = 142;
  /** RegularExpression Id. */
  int LT = 143;
  /** RegularExpression Id. */
  int LE = 144;
  /** RegularExpression Id. */
  int GE = 145;
  /** RegularExpression Id. */
  int BANG = 146;
  /** RegularExpression Id. */
  int TILDE = 147;
  /** RegularExpression Id. */
  int COLON = 148;
  /** RegularExpression Id. */
  int SC_OR = 149;
  /** RegularExpression Id. */
  int SC_AND = 150;
  /** RegularExpression Id. */
  int PLUS = 151;
  /** RegularExpression Id. */
  int MINUS = 152;
  /** RegularExpression Id. */
  int STAR = 153;
  /** RegularExpression Id. */
  int SLASH = 154;
  /** RegularExpression Id. */
  int DATATYPE = 155;
  /** RegularExpression Id. */
  int AT = 156;
  /** RegularExpression Id. */
  int ASSIGN = 157;
  /** RegularExpression Id. */
  int VBAR = 158;
  /** RegularExpression Id. */
  int CARAT = 159;
  /** RegularExpression Id. */
  int FPATH = 160;
  /** RegularExpression Id. */
  int RPATH = 161;
  /** RegularExpression Id. */
  int QMARK = 162;
  /** RegularExpression Id. */
  int PN_CHARS_BASE = 163;
  /** RegularExpression Id. */
  int PN_CHARS_U = 164;
  /** RegularExpression Id. */
  int PN_CHARS = 165;
  /** RegularExpression Id. */
  int PN_PREFIX = 166;
  /** RegularExpression Id. */
  int PN_LOCAL = 167;
  /** RegularExpression Id. */
  int VARNAME = 168;
  /** RegularExpression Id. */
  int UNKNOWN = 169;

  /** Lexical state. */
  int DEFAULT = 0;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\n\"",
    "\"\\r\"",
    "\"\\f\"",
    "<WS>",
    "<SINGLE_LINE_COMMENT>",
    "<IRIref>",
    "<PNAME_NS>",
    "<PNAME_LN>",
    "<BLANK_NODE_LABEL>",
    "<VAR1>",
    "<VAR2>",
    "<LANGTAG>",
    "<A2Z>",
    "<A2ZN>",
    "\"a\"",
    "\"base\"",
    "\"prefix\"",
    "\"select\"",
    "\"distinct\"",
    "\"reduced\"",
    "\"describe\"",
    "\"construct\"",
    "\"ask\"",
    "\"limit\"",
    "\"offset\"",
    "\"order\"",
    "\"by\"",
    "\"bindings\"",
    "\"undef\"",
    "\"asc\"",
    "\"desc\"",
    "\"named\"",
    "\"from\"",
    "\"where\"",
    "\"and\"",
    "\"graph\"",
    "\"optional\"",
    "\"union\"",
    "\"minus\"",
    "\"bind\"",
    "\"service\"",
    "\"let\"",
    "\"fetch\"",
    "\"exists\"",
    "\"not\"",
    "<NOTEXISTS>",
    "\"as\"",
    "\"group\"",
    "\"having\"",
    "\"separator\"",
    "\"agg\"",
    "\"count\"",
    "\"min\"",
    "\"max\"",
    "\"sum\"",
    "\"avg\"",
    "\"stdev\"",
    "\"sample\"",
    "\"group_concat\"",
    "\"filter\"",
    "\"bound\"",
    "\"coalesce\"",
    "\"in\"",
    "<NOT_IN>",
    "\"if\"",
    "\"bnode\"",
    "\"iri\"",
    "\"uri\"",
    "\"cast\"",
    "\"call\"",
    "\"str\"",
    "\"strlang\"",
    "\"strdt\"",
    "\"datatype\"",
    "\"lang\"",
    "\"langmatches\"",
    "\"isURI\"",
    "\"isIRI\"",
    "\"isBlank\"",
    "\"isLiteral\"",
    "\"isNumeric\"",
    "\"regex\"",
    "\"sameTerm\"",
    "\"rand\"",
    "\"true\"",
    "\"false\"",
    "\"data\"",
    "\"insert\"",
    "\"delete\"",
    "<INSERT_DATA>",
    "<DELETE_DATA>",
    "<DELETE_WHERE>",
    "\"modify\"",
    "\"load\"",
    "\"clear\"",
    "\"create\"",
    "\"add\"",
    "\"move\"",
    "\"copy\"",
    "\"meta\"",
    "\"silent\"",
    "\"drop\"",
    "\"into\"",
    "\"to\"",
    "\"default\"",
    "\"all\"",
    "\"with\"",
    "\"using\"",
    "<DIGITS>",
    "<INTEGER>",
    "<DECIMAL>",
    "<DOUBLE>",
    "<INTEGER_POSITIVE>",
    "<DECIMAL_POSITIVE>",
    "<DOUBLE_POSITIVE>",
    "<INTEGER_NEGATIVE>",
    "<DECIMAL_NEGATIVE>",
    "<DOUBLE_NEGATIVE>",
    "<EXPONENT>",
    "\"\\\"\\\"\\\"\"",
    "\"\\\'\\\'\\\'\"",
    "<ECHAR>",
    "<STRING_LITERAL1>",
    "<STRING_LITERAL2>",
    "<STRING_LITERAL_LONG1>",
    "<STRING_LITERAL_LONG2>",
    "\"(\"",
    "\")\"",
    "<NIL>",
    "\"{\"",
    "\"}\"",
    "\"[\"",
    "\"]\"",
    "<ANON>",
    "\";\"",
    "\",\"",
    "\".\"",
    "\"=\"",
    "\"!=\"",
    "\">\"",
    "\"<\"",
    "\"<=\"",
    "\">=\"",
    "\"!\"",
    "\"~\"",
    "\":\"",
    "\"||\"",
    "\"&&\"",
    "\"+\"",
    "\"-\"",
    "\"*\"",
    "\"/\"",
    "\"^^\"",
    "\"@\"",
    "\":=\"",
    "\"|\"",
    "\"^\"",
    "\"->\"",
    "\"<-\"",
    "\"?\"",
    "<PN_CHARS_BASE>",
    "<PN_CHARS_U>",
    "<PN_CHARS>",
    "<PN_PREFIX>",
    "<PN_LOCAL>",
    "<VARNAME>",
    "<UNKNOWN>",
  };

}