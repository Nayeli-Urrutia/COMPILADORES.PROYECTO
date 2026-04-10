# BerriLang — Gramática BNF/EBNF Formal

## Estructura general

```bnf
<programa>       ::= "nakama" ID "{" <declaraciones> <instrucciones> "}" "kaizoku"

<declaraciones>  ::= { <declaracion> }
<declaracion>    ::= <tipo> ID [ "=" <expresion> ] ";"
<tipo>           ::= "yoru" | "zoro" | "gomu" | "haki"

<instrucciones>  ::= { <instruccion> }
<instruccion>    ::= <asignacion>
                   | <condicional>
                   | <ciclo_while>
                   | <ciclo_for>
                   | <impresion>
                   | <retorno>
                   | <funcion_def>

<asignacion>     ::= ID "=" <expresion> ";"

<condicional>    ::= "luffy" "(" <expresion> ")" "{" <instrucciones> "}"
                     [ "sino" "{" <instrucciones> "}" ]

<ciclo_while>    ::= "chopper" "(" <expresion> ")" "{" <instrucciones> "}"

<ciclo_for>      ::= "robin" "(" (<declaracion>|<asignacion>) <expresion> ";" ID "=" <expresion> ")"
                     "{" <instrucciones> "}"

<impresion>      ::= "nami" "(" <expresion> ")" ";"
<retorno>        ::= "sanji" <expresion> ";"

<funcion_def>    ::= "usopp" ID "(" [ <params> ] ")" "{" <declaraciones> <instrucciones> "}"
<params>         ::= <tipo> ID { "," <tipo> ID }
```

## Expresiones (con precedencia)

```bnf
<expresion>      ::= <exprOr>
<exprOr>         ::= <exprAnd> { "||" <exprAnd> }
<exprAnd>        ::= <exprIgualdad> { "&&" <exprIgualdad> }
<exprIgualdad>   ::= <exprRelacional> { ( "==" | "!=" ) <exprRelacional> }
<exprRelacional> ::= <exprSuma> { ( "<" | ">" | "<=" | ">=" ) <exprSuma> }
<exprSuma>       ::= <exprMult> { ( "+" | "-" ) <exprMult> }
<exprMult>       ::= <exprUnaria> { ( "*" | "/" | "%" ) <exprUnaria> }
<exprUnaria>     ::= ( "!" | "-" ) <exprUnaria> | <primaria>
<primaria>       ::= ENTERO | DECIMAL | CADENA | "mera" | "kairyu" | ID
                   | "(" <expresion> ")"
```

## Tokens (Expresiones Regulares)

| Token         | Expresión Regular             |
|---------------|-------------------------------|
| ID            | `[a-zA-Z_][a-zA-Z0-9_]*`     |
| ENTERO        | `[0-9]+`                      |
| DECIMAL       | `[0-9]+\.[0-9]+`             |
| CADENA        | `"([^"\\]|\\.)*"`            |
| COMENTARIO    | `//[^\n]*` ó `/*.*?*/`       |
| OPERADOR_REL  | `<= >= == != < >`            |
| OPERADOR_LOG  | `\|\|` ó `&&` ó `!`          |
| OPERADOR_ARIT | `[+\-*/%]`                   |
| ASIGNAR       | `=`                           |
| DELIMITADORES | `[(){}:;,]`                  |
| FLECHA        | `->`                          |

## Palabras Reservadas (16)

| Palabra    | Equivalente en español | Uso                    |
|------------|------------------------|------------------------|
| nakama     | programa / inicio      | Inicio del programa    |
| kaizoku    | piratas / fin          | Fin del programa       |
| yoru       | espada (tipo entero)   | Declarar entero        |
| zoro       | Zoro (tipo decimal)    | Declarar decimal       |
| gomu       | goma (tipo cadena)     | Declarar cadena        |
| haki       | fuerza (tipo bool)     | Declarar booleano      |
| luffy      | si / condicional       | Condicional if         |
| sino       | sino / else            | Rama else              |
| chopper    | mientras               | Ciclo while            |
| robin      | para                   | Ciclo for              |
| nami       | imprimir               | Imprimir en consola    |
| usopp      | función                | Definir función        |
| sanji      | retornar               | Retornar valor         |
| denden     | leer                   | Leer entrada           |
| mera       | verdadero              | Literal true           |
| kairyu     | falso                  | Literal false          |
