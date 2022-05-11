parser grammar TarsParser;

options { 
tokenVocab=TarsLexer;
output = AST;
}

tokens
{COLON;COMMA;COMMENT;DOT;
EQ;ESC_SEQ;GT;HEX_DIGIT;LBRACE;LBRACKET;LPAREN;LT;
OCTAL_ESC; QUOTE; RBRACE; RBRACKET; RPAREN; SEMI; TARS_BOOL;
TARS_BYTE; TARS_CONST; TARS_DOUBLE; TARS_ENUM; TARS_FALSE;TARS_FLOAT;
TARS_FLOATING_POINT_LITERAL; TARS_IDENTIFIER;TARS_INCLUDE;TARS_INT;
TARS_INTEGER_LITERAL;TARS_INTERFACE;TARS_KEY;TARS_LONG;TARS_MAP;
TARS_NAMESPACE;TARS_OPTIONAL;TARS_OUT;TARS_REQUIRE;TARS_ROUTE_KEY;
TARS_SHORT;TARS_STRING;TARS_STRING_LITERAL;TARS_STRUCT;TARS_TRUE;
TARS_UNSIGNED;TARS_VECTOR;TARS_VOID;UNICODE_ESC;WS;TARS_OPERATION;
TARS_PARAM;TARS_REF;TARS_ROOT;TARS_STRUCT_MEMBER;}

@header {package com.qq.tars.maven.parse;}

start
 : include_def* namespace_def +
  -> ^( TARS_ROOT          (include_def )* ( namespace_def )+ ) ;


include_def 
 : TARS_INCLUDE TARS_STRING_LITERAL 
  -> ^( TARS_INCLUDE[$TARS_STRING_LITERAL.text] ) ;


namespace_def
 : TARS_NAMESPACE TARS_IDENTIFIER LBRACE (definition SEMI)+ RBRACE
  -> ^( TARS_NAMESPACE[$TARS_IDENTIFIER.text] ( definition )+ ) ;


definition
 : const_def | 
     enum_def | 
   struct_def | 
   key_def | 
   interface_def 
;

const_def
 : TARS_CONST type_primitive TARS_IDENTIFIER EQ v= const_initializer
  -> ^( TARS_CONST[$TARS_IDENTIFIER.text, $v.text] type_primitive ) ;
 
 
enum_def 
 :  TARS_ENUM n=TARS_IDENTIFIER LBRACE m+=TARS_IDENTIFIER (COMMA m+=TARS_IDENTIFIER)* COMMA? RBRACE 
  -> ^( TARS_ENUM[$n.text] ( $m)+ ) | 
   TARS_ENUM n=TARS_IDENTIFIER LBRACE m+=TARS_IDENTIFIER EQ v+=TARS_INTEGER_LITERAL (COMMA m+=TARS_IDENTIFIER EQ v+=TARS_INTEGER_LITERAL)* COMMA? RBRACE 
  -> ^( TARS_ENUM[$n.text] ( $m)+ ( $v)+ );


struct_def
 : TARS_STRUCT TARS_IDENTIFIER LBRACE (struct_member SEMI)+ RBRACE
  -> ^( TARS_STRUCT[$TARS_IDENTIFIER.text] ( struct_member )+ ) ;


struct_member
 : TARS_INTEGER_LITERAL (r= TARS_REQUIRE |r=TARS_OPTIONAL) type TARS_IDENTIFIER (EQ v= const_initializer )? 
  -> ^( TARS_STRUCT_MEMBER[$TARS_INTEGER_LITERAL.text, $r, $TARS_IDENTIFIER.text, $v.result] type ) ;


key_def
 :  TARS_KEY LBRACKET n=TARS_IDENTIFIER (COMMA k+= TARS_IDENTIFIER )+ RBRACKET 
  -> ^( TARS_KEY[$n.text] ( $k)+ ) ;


interface_def
 : TARS_INTERFACE TARS_IDENTIFIER LBRACE (operation SEMI)+ RBRACE
  -> ^( TARS_INTERFACE[$TARS_IDENTIFIER.text] ( operation )+ ) ;


operation
 : type TARS_IDENTIFIER LPAREN (param (COMMA param )* )? RPAREN
  -> ^( TARS_OPERATION[$TARS_IDENTIFIER.text] type               ( param )* );


param
 : TARS_ROUTE_KEY? TARS_OUT? type TARS_IDENTIFIER
  -> ^( TARS_PARAM[$TARS_IDENTIFIER.text, $TARS_OUT, $TARS_ROUTE_KEY] type ) ;


const_initializer returns [String result]
 : TARS_INTEGER_LITERAL |
   TARS_FLOATING_POINT_LITERAL |
   TARS_STRING_LITERAL |
   TARS_FALSE |
   TARS_TRUE;


type
 : type_primitive |
   type_vector | 
   type_map | 
   type_custom;
 

type_primitive
 : TARS_VOID 
  -> ^( TARS_VOID ) | 
   TARS_BOOL 
  -> ^( TARS_BOOL ) | 
   TARS_BYTE 
  -> ^( TARS_BYTE ) | 
   TARS_SHORT 
  -> ^( TARS_SHORT ) | 
   TARS_INT 
  -> ^( TARS_INT ) | 
   TARS_LONG 
  -> ^( TARS_LONG ) |
   TARS_FLOAT 
  -> ^( TARS_FLOAT ) | 
   TARS_DOUBLE
  -> ^( TARS_DOUBLE ) | 
   TARS_STRING 
  -> ^( TARS_STRING ) | 
   TARS_UNSIGNED TARS_INT 
  -> ^( TARS_LONG );


type_vector
 : TARS_VECTOR LT type GT 
  -> ^( TARS_VECTOR type ) ;
  
  
type_map
 : TARS_MAP LT type COMMA type GT 
  -> ^( TARS_MAP type type ) ;
  
  
type_custom 
 : TARS_IDENTIFIER 
  -> ^( TARS_MAP[$TARS_IDENTIFIER.text] ) |
   ns= TARS_IDENTIFIER COLON COLON id= TARS_IDENTIFIER 
  -> ^( TARS_REF[$ns.text,$id.text] ) ;