// Grammar for Norn mailing list expressions

@skip whitespace {
    parallel ::= sequence ('|' sequence)* ;
    sequence ::= union (';' union)* ;
    union ::= definition (',' definition)* ;
    definition ::= (listname '=' union) | difference ;
    difference ::= intersection ('!' intersection)* ;
    intersection ::= base ('*' base)* ;
    base ::= recipient | listname | '(' parallel ')' | empty ;
}
recipient ::= ([A-Za-z0-9_.+-]+)'@'([A-Za-z0-9_.-]+) ;
listname ::= [A-Za-z0-9_.-]+ ;
empty ::= [ \t\r\n]? ;
whitespace ::= [ \t\r\n]+ ;