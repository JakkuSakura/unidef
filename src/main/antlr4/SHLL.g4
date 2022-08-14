grammar SHLL;
IDENT: [a-zA-Z_][a-zA-Z0-9_]*;
INTEGER: '0' [Xx] [a-zA-Z0-9]+
       | '0' [Oo] [0-7]+
       | '0' [Bb] [0-1]+
       | [+-]? '0'
       | [+-]? [1-9][0-9]*;

DECIMAL: [+-]? [0-9]+ '.' [0-9]+;

STRING: '"' ([^"]|'\\"')* '"';
CHAR: '\'' ([^"]|'\\"'|'\\'.+?) '\'';
WS : (' ' | '\t' | '\n' )+ -> skip;

term: apply | IDENT | INTEGER | DECIMAL | STRING | CHAR ;
kw_arg: IDENT '=' term;
kw_args: kw_arg *;
pos_args: term *;
apply: '(' term pos_args kw_args ')';
