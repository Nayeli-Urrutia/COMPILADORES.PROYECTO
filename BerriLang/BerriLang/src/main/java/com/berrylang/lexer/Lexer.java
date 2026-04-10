package com.berrylang.lexer;

import com.berrylang.model.ErrorEntry;
import com.berrylang.model.Token;
import com.berrylang.model.Token.TokenType;

import java.util.*;

public class Lexer {

    // ── Palabras reservadas ──────────────────────────────────────────────
    private static final Map<String, TokenType> RESERVADAS = new HashMap<>();
    static {
        RESERVADAS.put("nakama",   TokenType.NAKAMA);
        RESERVADAS.put("kaizoku",  TokenType.KAIZOKU);
        RESERVADAS.put("yoru",     TokenType.YORU);
        RESERVADAS.put("zoro",     TokenType.ZORO);
        RESERVADAS.put("gomu",     TokenType.GOMU);
        RESERVADAS.put("haki",     TokenType.HAKI);
        RESERVADAS.put("luffy",    TokenType.LUFFY);
        RESERVADAS.put("nami",     TokenType.NAMI);
        RESERVADAS.put("denden",   TokenType.DENDEN);
        RESERVADAS.put("chopper",  TokenType.CHOPPER);
        RESERVADAS.put("robin",    TokenType.ROBIN);
        RESERVADAS.put("sanji",    TokenType.SANJI);
        RESERVADAS.put("usopp",    TokenType.USOPP);
        RESERVADAS.put("sino",     TokenType.NAMI_ELSE);   // else
        RESERVADAS.put("mera",     TokenType.MERA);
        RESERVADAS.put("kairyu",   TokenType.KAIRYU);
    }

    private final String fuente;
    private int pos    = 0;
    private int linea  = 1;
    private int columna = 1;

    private final List<Token>      tokens  = new ArrayList<>();
    private final List<ErrorEntry> errores = new ArrayList<>();

    public Lexer(String fuente) {
        this.fuente = fuente;
    }

    // ── API pública ──────────────────────────────────────────────────────
    public List<Token>      getTokens()  { return tokens; }
    public List<ErrorEntry> getErrores() { return errores; }

    public void analizar() {
        tokens.clear();
        errores.clear();
        pos = 0; linea = 1; columna = 1;

        while (pos < fuente.length()) {
            omitirEspacios();
            if (pos >= fuente.length()) break;

            char c = fuente.charAt(pos);

            // Comentario de línea
            if (c == '/' && siguiente('/')) {
                omitirLineaComentario(); continue;
            }
            // Comentario bloque
            if (c == '/' && siguiente('*')) {
                omitirBloqueComentario(); continue;
            }

            if (Character.isLetter(c) || c == '_') { leerIdentificadorOReservada(); continue; }
            if (Character.isDigit(c))               { leerNumero(); continue; }
            if (c == '"')                           { leerCadena(); continue; }

            leerSimbolo();
        }

        tokens.add(new Token(TokenType.EOF, "EOF", linea, columna));
    }

    // ── Lecturas específicas ─────────────────────────────────────────────

    private void leerIdentificadorOReservada() {
        int colInicio = columna;
        int lineaInicio = linea;
        StringBuilder sb = new StringBuilder();
        while (pos < fuente.length() && (Character.isLetterOrDigit(fuente.charAt(pos)) || fuente.charAt(pos) == '_')) {
            sb.append(avanzar());
        }
        String word = sb.toString();
        TokenType tipo = RESERVADAS.getOrDefault(word, TokenType.ID);
        tokens.add(new Token(tipo, word, lineaInicio, colInicio));
    }

    private void leerNumero() {
        int colInicio = columna;
        int lineaInicio = linea;
        StringBuilder sb = new StringBuilder();
        while (pos < fuente.length() && Character.isDigit(fuente.charAt(pos))) sb.append(avanzar());
        if (pos < fuente.length() && fuente.charAt(pos) == '.' && pos+1 < fuente.length() && Character.isDigit(fuente.charAt(pos+1))) {
            sb.append(avanzar()); // '.'
            while (pos < fuente.length() && Character.isDigit(fuente.charAt(pos))) sb.append(avanzar());
            tokens.add(new Token(TokenType.DECIMAL, sb.toString(), lineaInicio, colInicio));
        } else {
            tokens.add(new Token(TokenType.ENTERO, sb.toString(), lineaInicio, colInicio));
        }
    }

    private void leerCadena() {
        int colInicio = columna;
        int lineaInicio = linea;
        avanzar(); // consume "
        StringBuilder sb = new StringBuilder();
        while (pos < fuente.length() && fuente.charAt(pos) != '"') {
            char c = fuente.charAt(pos);
            if (c == '\n') {
                errores.add(new ErrorEntry(ErrorEntry.TipoError.LEXICO,
                        "Cadena sin cerrar", lineaInicio, colInicio, "Falta la comilla de cierre \""));
                return;
            }
            if (c == '\\' && pos+1 < fuente.length()) { sb.append(avanzar()); }
            sb.append(avanzar());
        }
        if (pos >= fuente.length()) {
            errores.add(new ErrorEntry(ErrorEntry.TipoError.LEXICO,
                    "Cadena sin cerrar", lineaInicio, colInicio, "Se llegó al final sin comilla de cierre"));
            return;
        }
        avanzar(); // consume "
        tokens.add(new Token(TokenType.CADENA, sb.toString(), lineaInicio, colInicio));
    }

    private void leerSimbolo() {
        int col  = columna;
        int lin  = linea;
        char c   = avanzar();

        switch (c) {
            case '+' -> tokens.add(new Token(TokenType.MAS,         "+", lin, col));
            case '-' -> {
                if (pos < fuente.length() && fuente.charAt(pos) == '>') {
                    avanzar();
                    tokens.add(new Token(TokenType.FLECHA, "->", lin, col));
                } else {
                    tokens.add(new Token(TokenType.MENOS, "-", lin, col));
                }
            }
            case '*' -> tokens.add(new Token(TokenType.POR,         "*", lin, col));
            case '/' -> tokens.add(new Token(TokenType.DIVIDIR,     "/", lin, col));
            case '%' -> tokens.add(new Token(TokenType.MODULO,      "%", lin, col));
            case '(' -> tokens.add(new Token(TokenType.PAREN_ABRE,  "(", lin, col));
            case ')' -> tokens.add(new Token(TokenType.PAREN_CIERRA,")", lin, col));
            case '{' -> tokens.add(new Token(TokenType.LLAVE_ABRE,  "{", lin, col));
            case '}' -> tokens.add(new Token(TokenType.LLAVE_CIERRA,"}", lin, col));
            case ';' -> tokens.add(new Token(TokenType.PUNTO_COMA,  ";", lin, col));
            case ':' -> tokens.add(new Token(TokenType.DOS_PUNTOS,  ":", lin, col));
            case ',' -> tokens.add(new Token(TokenType.COMA,        ",", lin, col));
            case '=' -> {
                if (pos < fuente.length() && fuente.charAt(pos) == '=') {
                    avanzar();
                    tokens.add(new Token(TokenType.IGUAL_IGUAL, "==", lin, col));
                } else {
                    tokens.add(new Token(TokenType.ASIGNAR, "=", lin, col));
                }
            }
            case '!' -> {
                if (pos < fuente.length() && fuente.charAt(pos) == '=') {
                    avanzar();
                    tokens.add(new Token(TokenType.DIFERENTE, "!=", lin, col));
                } else {
                    tokens.add(new Token(TokenType.NO, "!", lin, col));
                }
            }
            case '<' -> {
                if (pos < fuente.length() && fuente.charAt(pos) == '=') {
                    avanzar();
                    tokens.add(new Token(TokenType.MENOR_IGUAL, "<=", lin, col));
                } else {
                    tokens.add(new Token(TokenType.MENOR, "<", lin, col));
                }
            }
            case '>' -> {
                if (pos < fuente.length() && fuente.charAt(pos) == '=') {
                    avanzar();
                    tokens.add(new Token(TokenType.MAYOR_IGUAL, ">=", lin, col));
                } else {
                    tokens.add(new Token(TokenType.MAYOR, ">", lin, col));
                }
            }
            case '&' -> {
                if (pos < fuente.length() && fuente.charAt(pos) == '&') {
                    avanzar();
                    tokens.add(new Token(TokenType.Y, "&&", lin, col));
                } else {
                    registrarError("Carácter inválido '&'", lin, col, "Se esperaba '&&'");
                }
            }
            case '|' -> {
                if (pos < fuente.length() && fuente.charAt(pos) == '|') {
                    avanzar();
                    tokens.add(new Token(TokenType.O, "||", lin, col));
                } else {
                    registrarError("Carácter inválido '|'", lin, col, "Se esperaba '||'");
                }
            }
            default -> registrarError("Carácter no permitido '" + c + "'", lin, col,
                        "El carácter '" + c + "' no pertenece al lenguaje BerriLang");
        }
    }

    // ── Utilidades ───────────────────────────────────────────────────────

    private char avanzar() {
        char c = fuente.charAt(pos++);
        if (c == '\n') { linea++; columna = 1; } else { columna++; }
        return c;
    }

    private boolean siguiente(char expected) {
        return pos + 1 < fuente.length() && fuente.charAt(pos + 1) == expected;
    }

    private void omitirEspacios() {
        while (pos < fuente.length() && Character.isWhitespace(fuente.charAt(pos))) avanzar();
    }

    private void omitirLineaComentario() {
        while (pos < fuente.length() && fuente.charAt(pos) != '\n') avanzar();
    }

    private void omitirBloqueComentario() {
        avanzar(); avanzar(); // consume /*
        while (pos < fuente.length()) {
            char c = avanzar();
            if (c == '*' && pos < fuente.length() && fuente.charAt(pos) == '/') {
                avanzar(); return;
            }
        }
        errores.add(new ErrorEntry(ErrorEntry.TipoError.LEXICO,
                "Comentario de bloque sin cerrar", linea, columna, "Falta */"));
    }

    private void registrarError(String desc, int lin, int col, String causa) {
        errores.add(new ErrorEntry(ErrorEntry.TipoError.LEXICO, desc, lin, col, causa));
        tokens.add(new Token(TokenType.ERROR, String.valueOf(fuente.charAt(pos-1)), lin, col));
    }
}
