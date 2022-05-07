grammar Taf;

tokens
{
COLON;COMMA;COMMENT;DOT;
EQ;ESC_SEQ;GT;HEX_DIGIT;LBRACE;LBRACKET;LPAREN;LT;
OCTAL_ESC; QUOTE; RBRACE; RBRACKET; RPAREN; SEMI; TAF_BOOL;
TAF_BYTE; TAF_CONST; TAF_DOUBLE; TAF_ENUM; TAF_FALSE;TAF_FLOAT;
TAF_FLOATING_POINT_LITERAL; TAF_IDENTIFIER;TAF_INCLUDE;TAF_INT;
TAF_INTEGER_LITERAL;TAF_INTERFACE;TAF_KEY;TAF_LONG;TAF_MAP;
TAF_NAMESPACE;TAF_OPTIONAL;TAF_OUT;TAF_REQUIRE;TAF_ROUTE_KEY;
TAF_SHORT;TAF_STRING;TAF_STRING_LITERAL;TAF_STRUCT;TAF_TRUE;
TAF_UNSIGNED;TAF_VECTOR;TAF_VOID;UNICODE_ESC;WS;TAF_OPERATION;
TAF_PARAM;TAF_REF;TAF_ROOT;TAF_STRUCT_MEMBER;
}






start :
 ( include_def )* ( namespace_def )+ ;



include_def :
 Taf_INCLUDE Taf_STRING_LITERAL ;



namespace_def :
 Taf_NAMESPACE Taf_IDENTIFIER LBRACE ( definition SEMI )+ RBRACE;



definition :
 ( const_def |
   enum_def |
   struct_def |
   key_def |
   interface_def
);

const_def :
 Taf_CONST type_primitive Taf_IDENTIFIER EQ v= const_initializer ;



enum_def :
 ( Taf_ENUM Taf_IDENTIFIER LBRACE Taf_IDENTIFIER ( COMMA Taf_IDENTIFIER )* ( COMMA )? RBRACE );





struct_def :
 Taf_STRUCT Taf_IDENTIFIER LBRACE ( struct_member SEMI )+ RBRACE ;



struct_member :
 Taf_INTEGER_LITERAL (r= Taf_REQUIRE |r= Taf_OPTIONAL ) type Taf_IDENTIFIER ( EQ v= const_initializer )? ;



key_def :
 Taf_KEY LBRACKET n= Taf_IDENTIFIER ( COMMA k+= Taf_IDENTIFIER )+ RBRACKET ;



interface_def :
 Taf_INTERFACE Taf_IDENTIFIER LBRACE ( operation SEMI )+ RBRACE ;



operation :
 type Taf_IDENTIFIER LPAREN ( param ( COMMA param )* )? RPAREN ;



param :
 ( Taf_ROUTE_KEY )? ( Taf_OUT )? type Taf_IDENTIFIER ;











type :
 ( type_primitive |
 type_vector |
 type_map |
 type_custom
 );

type_primitive :
 ( TAF_VOID);

type_vector : Taf_VECTOR LT type GT ;
type_map : Taf_MAP LT type COMMA type GT ;
type_custom : ( Taf_IDENTIFIER ) ;
const_initializer : ( Taf_INTEGER_LITERAL | Taf_FLOATING_POINT_LITERAL | Taf_STRING_LITERAL | Taf_FALSE | Taf_TRUE );

