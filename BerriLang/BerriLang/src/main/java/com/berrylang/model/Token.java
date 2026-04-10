package com.berrylang.model;

/**
 * BerriLang - Representación de un Token
 * Temática: One Piece
 */
public class Token {

    public enum TokenType {
        // Palabras reservadas
        NAKAMA, KAIZOKU,           // programa / fin
        YORU, ZORO, GOMU, HAKI,   // tipos: int, float, string, bool
        LUFFY, NAMI_ELSE,          // if / else
        CHOPPER,                   // while
        ROBIN,                     // for
        SANJI,                     // return
        USOPP,                     // función
        NAMI,                      // print
        DENDEN,                    // input
        MERA, KAIRYU,              // true / false

        // Literales
        ENTERO, DECIMAL, CADENA, BOOLEANO,

        // Identificadores
        ID,

        // Operadores aritméticos
        MAS, MENOS, POR, DIVIDIR, MODULO,

        // Operadores de comparación
        IGUAL_IGUAL, DIFERENTE, MENOR, MAYOR, MENOR_IGUAL, MAYOR_IGUAL,

        // Operadores lógicos
        Y, O, NO,

        // Asignación
        ASIGNAR,

        // Delimitadores
        PAREN_ABRE, PAREN_CIERRA,
        LLAVE_ABRE, LLAVE_CIERRA,
        PUNTO_COMA, DOS_PUNTOS, COMA,
        FLECHA,   // ->

        // Especial
        EOF, ERROR
    }

    private final TokenType tipo;
    private final String valor;
    private final int linea;
    private final int columna;

    public Token(TokenType tipo, String valor, int linea, int columna) {
        this.tipo = tipo;
        this.valor = valor;
        this.linea = linea;
        this.columna = columna;
    }

    public TokenType getTipo()   { return tipo; }
    public String    getValor()  { return valor; }
    public int       getLinea()  { return linea; }
    public int       getColumna(){ return columna; }

    @Override
    public String toString() {
        return String.format("Token[%s, \"%s\", L%d:C%d]", tipo, valor, linea, columna);
    }
}
