lexer grammar TarsLexer;

@header {
package com.qq.tars.maven.parse;
}

TARS_VOID
 : 'void';

TARS_STRUCT
 : 'struct';

TARS_UNSIGNED
 : 'unsigned';

TARS_BOOL
 : 'bool';

TARS_BYTE
 : 'byte';

TARS_SHORT
 : 'short';

TARS_INT
 : 'int';

TARS_DOUBLE
 : 'double';

TARS_FLOAT
 : 'float';

TARS_LONG
 : 'long' ;

TARS_STRING
 : 'string' ;

TARS_VECTOR
 : 'vector' ;

TARS_MAP
 : 'map' ;

TARS_KEY
 : 'key' ;

TARS_ROUTE_KEY
 : 'routekey' ;

TARS_INCLUDE
 : '#include' ;

TARS_NAMESPACE
 : 'module' ;

TARS_INTERFACE
 : 'interface' ;

TARS_OUT
 : 'out' ;

TARS_REQUIRE
 : 'require' ;

TARS_OPTIONAL
 : 'optional' ;

TARS_FALSE
 : 'false' ;

TARS_TRUE
 : 'true' ;

TARS_ENUM
 : 'enum' ;

TARS_CONST
 : 'const' ;



TARS_IDENTIFIER
    : ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ;


TARS_INTEGER_LITERAL
    : ('0X'|'0x')('0'..'9'|'a'..'f'|'A'..'F')+|
      ('O'|'o')('0'..'7')+|
      ('+'|'-')?('0'..'9')+;


TARS_FLOATING_POINT_LITERAL
    :   ('+'|'-')? ('0'..'9')+ '.' ('0'..'9')+;


TARS_STRING_LITERAL
    : '"' ( ESC_SEQ | ~ ( '\\' | '"' ) )* '"' ;


LPAREN
    :   '(' ;


RPAREN
    :   ')' ;


LBRACE
    :   '{' ;


RBRACE
    :   '}' ;


LBRACKET
    :   '[' ;


RBRACKET
    :   ']' ;


SEMI
 : ';' ;


COMMA
 : ',' ;


QUOTE
 : '"' ;


DOT
 : '.' ;


COLON
    :   ':' ;


EQ
 : '=' ;


GT
 : '>' ;


LT
 : '<' ;


COMMENT
	@init{
		boolean isDoc = false;
	}
    :   '//' ~('\n'|'\r')* '\r'? '\n' {skip();}|
        '/*'
            {
                if((char)input.LA(1) == '*'){
                    isDoc = true;
                }
            }
        ( options {greedy=false; }:. )* '*/'
            {
                if(isDoc==true){
                    _channel=HIDDEN;
                } else{
                    skip();
                }
            };


WS
    :   (' ' | '\t' | '\r' | '\n')  {skip();} ;






fragment HEX_DIGIT
          : ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' ) ;


fragment ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    |   UNICODE_ESC
    |   OCTAL_ESC
    ;


fragment OCTAL_ESC
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;


fragment UNICODE_ESC
    :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;