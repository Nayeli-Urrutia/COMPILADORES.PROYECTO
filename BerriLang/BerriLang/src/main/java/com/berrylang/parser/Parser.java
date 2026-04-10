package com.berrylang.parser;

import com.berrylang.model.ErrorEntry;
import com.berrylang.model.ErrorEntry.TipoError;
import com.berrylang.model.SimboloEntry;
import com.berrylang.model.Token;
import com.berrylang.model.Token.TokenType;

import java.util.ArrayList;
import java.util.List;

/**
 * BerriLang - Analizador Sintáctico (Descendente Recursivo)
 *
 * Gramática BNF resumida:
 *
 * programa      -> nakama ID { declaraciones instrucciones } kaizoku
 * declaraciones -> (declaracion)*
 * declaracion   -> tipo ID (= expresion)? ;
 * tipo          -> yoru | zoro | gomu | haki
 * instrucciones -> (instruccion)*
 * instruccion   -> asignacion | condicional | cicloWhile | cicloFor
 *                | retorno | impresion | llamadaFuncion | funcionDef
 * asignacion    -> ID = expresion ;
 * condicional   -> luffy ( expresion ) { instrucciones } (sino { instrucciones })?
 * cicloWhile    -> chopper ( expresion ) { instrucciones }
 * cicloFor      -> robin ( asignacion ; expresion ; asignacion ) { instrucciones }
 * impresion     -> nami ( expresion ) ;
 * retorno       -> sanji expresion ;
 * funcionDef    -> usopp ID ( params? ) { instrucciones }
 * expresion     -> exprOr
 * exprOr        -> exprAnd (|| exprAnd)*
 * exprAnd       -> exprIgualdad (&& exprIgualdad)*
 * exprIgualdad  -> exprRelacional ((== | !=) exprRelacional)*
 * exprRelacional-> exprSuma ((< | > | <= | >=) exprSuma)*
 * exprSuma      -> exprMult ((+ | -) exprMult)*
 * exprMult      -> exprUnaria ((* | / | %) exprUnaria)*
 * exprUnaria    -> (! | -) exprUnaria | primaria
 * primaria      -> ENTERO | DECIMAL | CADENA | mera | kairyu | ID | ( expresion )
 */
public class Parser {

    private final List<Token>       tokens;
    private final List<ErrorEntry>  errores  = new ArrayList<>();
    private final List<SimboloEntry> simbolos = new ArrayList<>();
    private int pos = 0;
    private String ambitoActual = "global";

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<ErrorEntry>   getErrores()  { return errores; }
    public List<SimboloEntry> getSimbolos() { return simbolos; }

    // ── API ──────────────────────────────────────────────────────────────

    public NodoAST parsear() {
        NodoAST raiz = parsePrograma();
        if (!esEOF()) {
            errorSint("Tokens inesperados después de 'kaizoku'", actual());
        }
        return raiz;
    }

    // ── Reglas de producción ─────────────────────────────────────────────

    private NodoAST parsePrograma() {
        NodoAST nodo = new NodoAST("Programa", "");
        if (!consumir(TokenType.NAKAMA)) {
            errorSint("Se esperaba 'nakama' para iniciar el programa", actual());
            return nodo;
        }
        Token nombreProg = actual();
        if (!consumir(TokenType.ID)) {
            errorSint("Se esperaba el nombre del programa después de 'nakama'", actual());
        } else {
            nodo = new NodoAST("Programa", nombreProg.getValor(), nombreProg.getLinea(), nombreProg.getColumna());
        }
        if (!consumir(TokenType.LLAVE_ABRE)) {
            errorSint("Se esperaba '{' para abrir el cuerpo del programa", actual());
        }
        nodo.agregarHijo(parseDeclaraciones());
        nodo.agregarHijo(parseInstrucciones());
        if (!consumir(TokenType.LLAVE_CIERRA)) {
            errorSint("Se esperaba '}' para cerrar el programa", actual());
        }
        if (!consumir(TokenType.KAIZOKU)) {
            errorSint("Se esperaba 'kaizoku' para finalizar el programa", actual());
        }
        return nodo;
    }

    private NodoAST parseDeclaraciones() {
        NodoAST nodo = new NodoAST("Declaraciones", "");
        while (esTipo(actual().getTipo())) {
            nodo.agregarHijo(parseDeclaracion());
        }
        return nodo;
    }

    private NodoAST parseDeclaracion() {
        NodoAST nodo = new NodoAST("Declaracion", "");
        Token tipoTok = actual();
        String tipoStr = tipoTok.getValor();
        avanzar();

        Token idTok = actual();
        if (!consumir(TokenType.ID)) {
            errorSint("Se esperaba un identificador en la declaración", actual()); return nodo;
        }
        nodo = new NodoAST("Declaracion", tipoStr + " " + idTok.getValor(), idTok.getLinea(), idTok.getColumna());

        // Registrar en tabla de símbolos
        simbolos.add(new SimboloEntry(idTok.getValor(), tipoStr, "indefinido",
                idTok.getLinea(), idTok.getColumna(), ambitoActual));

        if (esTipo(actual().getTipo()) || actual().getTipo() == TokenType.ASIGNAR) {
            if (consumir(TokenType.ASIGNAR)) {
                NodoAST expr = parseExpresion();
                nodo.agregarHijo(expr);
                // actualizar valor en tabla
                actualizarValorSimbolo(idTok.getValor(), expr.getValor());
            }
        }
        if (!consumir(TokenType.PUNTO_COMA)) {
            errorSint("Se esperaba ';' al final de la declaración", actual());
        }
        return nodo;
    }

    private NodoAST parseInstrucciones() {
        NodoAST nodo = new NodoAST("Instrucciones", "");
        while (!esFinBloque() && !esEOF()) {
            NodoAST inst = parseInstruccion();
            if (inst != null) nodo.agregarHijo(inst);
        }
        return nodo;
    }

    private NodoAST parseInstruccion() {
        Token t = actual();
        return switch (t.getTipo()) {
            case ID      -> parseAsignacion();
            case LUFFY   -> parseCondicional();
            case CHOPPER -> parseCicloWhile();
            case ROBIN   -> parseCicloFor();
            case NAMI    -> parseImpresion();
            case SANJI   -> parseRetorno();
            case USOPP   -> parseFuncionDef();
            default -> {
                errorSint("Instrucción no reconocida: '" + t.getValor() + "'", t);
                avanzar();
                yield null;
            }
        };
    }

    private NodoAST parseAsignacion() {
        Token id = actual(); avanzar();
        NodoAST nodo = new NodoAST("Asignacion", id.getValor(), id.getLinea(), id.getColumna());
        if (!consumir(TokenType.ASIGNAR)) {
            errorSint("Se esperaba '=' en la asignación", actual()); return nodo;
        }
        nodo.agregarHijo(parseExpresion());
        if (!consumir(TokenType.PUNTO_COMA)) {
            errorSint("Se esperaba ';' al final de la asignación", actual());
        }
        return nodo;
    }

    private NodoAST parseCondicional() {
        Token luffyTok = actual(); avanzar();
        NodoAST nodo = new NodoAST("Si", "luffy", luffyTok.getLinea(), luffyTok.getColumna());
        if (!consumir(TokenType.PAREN_ABRE))  errorSint("Se esperaba '(' en 'luffy'", actual());
        nodo.agregarHijo(parseExpresion());
        if (!consumir(TokenType.PAREN_CIERRA)) errorSint("Se esperaba ')' en 'luffy'", actual());
        if (!consumir(TokenType.LLAVE_ABRE))   errorSint("Se esperaba '{' en 'luffy'", actual());
        nodo.agregarHijo(parseInstrucciones());
        if (!consumir(TokenType.LLAVE_CIERRA)) errorSint("Se esperaba '}' en 'luffy'", actual());

        if (actual().getTipo() == TokenType.NAMI_ELSE) {
            avanzar();
            NodoAST siNo = new NodoAST("Sino", "sino");
            if (!consumir(TokenType.LLAVE_ABRE))  errorSint("Se esperaba '{' en 'sino'", actual());
            siNo.agregarHijo(parseInstrucciones());
            if (!consumir(TokenType.LLAVE_CIERRA)) errorSint("Se esperaba '}' en 'sino'", actual());
            nodo.agregarHijo(siNo);
        }
        return nodo;
    }

    private NodoAST parseCicloWhile() {
        Token tok = actual(); avanzar();
        NodoAST nodo = new NodoAST("Mientras", "chopper", tok.getLinea(), tok.getColumna());
        if (!consumir(TokenType.PAREN_ABRE))   errorSint("Se esperaba '(' en 'chopper'", actual());
        nodo.agregarHijo(parseExpresion());
        if (!consumir(TokenType.PAREN_CIERRA)) errorSint("Se esperaba ')' en 'chopper'", actual());
        if (!consumir(TokenType.LLAVE_ABRE))   errorSint("Se esperaba '{' en 'chopper'", actual());
        nodo.agregarHijo(parseInstrucciones());
        if (!consumir(TokenType.LLAVE_CIERRA)) errorSint("Se esperaba '}' en 'chopper'", actual());
        return nodo;
    }

    private NodoAST parseCicloFor() {
        Token tok = actual(); avanzar();
        NodoAST nodo = new NodoAST("Para", "robin", tok.getLinea(), tok.getColumna());
        if (!consumir(TokenType.PAREN_ABRE)) errorSint("Se esperaba '(' en 'robin'", actual());
        // init
        if (esTipo(actual().getTipo())) nodo.agregarHijo(parseDeclaracion());
        else nodo.agregarHijo(parseAsignacion());
        // condición
        nodo.agregarHijo(parseExpresion());
        if (!consumir(TokenType.PUNTO_COMA)) errorSint("Se esperaba ';' en 'robin'", actual());
        // incremento - asignacion sin punto y coma
        Token id2 = actual(); avanzar();
        NodoAST inc = new NodoAST("Asignacion", id2.getValor());
        if (!consumir(TokenType.ASIGNAR)) errorSint("Se esperaba '=' en incremento de 'robin'", actual());
        inc.agregarHijo(parseExpresion());
        nodo.agregarHijo(inc);
        if (!consumir(TokenType.PAREN_CIERRA)) errorSint("Se esperaba ')' en 'robin'", actual());
        if (!consumir(TokenType.LLAVE_ABRE))   errorSint("Se esperaba '{' en 'robin'", actual());
        nodo.agregarHijo(parseInstrucciones());
        if (!consumir(TokenType.LLAVE_CIERRA)) errorSint("Se esperaba '}' en 'robin'", actual());
        return nodo;
    }

    private NodoAST parseImpresion() {
        Token tok = actual(); avanzar();
        NodoAST nodo = new NodoAST("Imprimir", "nami", tok.getLinea(), tok.getColumna());
        if (!consumir(TokenType.PAREN_ABRE))   errorSint("Se esperaba '(' en 'nami'", actual());
        nodo.agregarHijo(parseExpresion());
        if (!consumir(TokenType.PAREN_CIERRA)) errorSint("Se esperaba ')' en 'nami'", actual());
        if (!consumir(TokenType.PUNTO_COMA))   errorSint("Se esperaba ';' después de 'nami(...)'", actual());
        return nodo;
    }

    private NodoAST parseRetorno() {
        Token tok = actual(); avanzar();
        NodoAST nodo = new NodoAST("Retorno", "sanji", tok.getLinea(), tok.getColumna());
        nodo.agregarHijo(parseExpresion());
        if (!consumir(TokenType.PUNTO_COMA)) errorSint("Se esperaba ';' después de 'sanji'", actual());
        return nodo;
    }

    private NodoAST parseFuncionDef() {
        Token tok = actual(); avanzar();
        Token idTok = actual();
        if (!consumir(TokenType.ID)) { errorSint("Se esperaba nombre de función después de 'usopp'", actual()); return new NodoAST("Funcion",""); }
        NodoAST nodo = new NodoAST("Funcion", idTok.getValor(), idTok.getLinea(), idTok.getColumna());
        String prevAmbito = ambitoActual;
        ambitoActual = idTok.getValor();
        if (!consumir(TokenType.PAREN_ABRE)) errorSint("Se esperaba '(' en definición de función", actual());
        // parámetros opcionales
        while (actual().getTipo() != TokenType.PAREN_CIERRA && !esEOF()) {
            Token tipoP = actual(); avanzar();
            Token idP   = actual();
            if (!consumir(TokenType.ID)) { errorSint("Parámetro inválido", actual()); break; }
            nodo.agregarHijo(new NodoAST("Param", tipoP.getValor() + " " + idP.getValor()));
            simbolos.add(new SimboloEntry(idP.getValor(), tipoP.getValor(), "param",
                    idP.getLinea(), idP.getColumna(), ambitoActual));
            if (actual().getTipo() == TokenType.COMA) avanzar();
        }
        if (!consumir(TokenType.PAREN_CIERRA)) errorSint("Se esperaba ')' en función", actual());
        if (!consumir(TokenType.LLAVE_ABRE))   errorSint("Se esperaba '{' en función", actual());
        nodo.agregarHijo(parseDeclaraciones());
        nodo.agregarHijo(parseInstrucciones());
        if (!consumir(TokenType.LLAVE_CIERRA)) errorSint("Se esperaba '}' para cerrar función", actual());
        ambitoActual = prevAmbito;
        return nodo;
    }

    // ── Expresiones (precedencia de operadores) ──────────────────────────

    private NodoAST parseExpresion() { return parseExprOr(); }

    private NodoAST parseExprOr() {
        NodoAST izq = parseExprAnd();
        while (actual().getTipo() == TokenType.O) {
            Token op = actual(); avanzar();
            NodoAST der  = parseExprAnd();
            NodoAST nodo = new NodoAST("BinOp", "||", op.getLinea(), op.getColumna());
            nodo.agregarHijo(izq); nodo.agregarHijo(der);
            izq = nodo;
        }
        return izq;
    }

    private NodoAST parseExprAnd() {
        NodoAST izq = parseExprIgualdad();
        while (actual().getTipo() == TokenType.Y) {
            Token op = actual(); avanzar();
            NodoAST der  = parseExprIgualdad();
            NodoAST nodo = new NodoAST("BinOp", "&&", op.getLinea(), op.getColumna());
            nodo.agregarHijo(izq); nodo.agregarHijo(der);
            izq = nodo;
        }
        return izq;
    }

    private NodoAST parseExprIgualdad() {
        NodoAST izq = parseExprRelacional();
        while (actual().getTipo() == TokenType.IGUAL_IGUAL || actual().getTipo() == TokenType.DIFERENTE) {
            Token op = actual(); avanzar();
            NodoAST der  = parseExprRelacional();
            NodoAST nodo = new NodoAST("BinOp", op.getValor(), op.getLinea(), op.getColumna());
            nodo.agregarHijo(izq); nodo.agregarHijo(der);
            izq = nodo;
        }
        return izq;
    }

    private NodoAST parseExprRelacional() {
        NodoAST izq = parseExprSuma();
        while (esRelacional(actual().getTipo())) {
            Token op = actual(); avanzar();
            NodoAST der  = parseExprSuma();
            NodoAST nodo = new NodoAST("BinOp", op.getValor(), op.getLinea(), op.getColumna());
            nodo.agregarHijo(izq); nodo.agregarHijo(der);
            izq = nodo;
        }
        return izq;
    }

    private NodoAST parseExprSuma() {
        NodoAST izq = parseExprMult();
        while (actual().getTipo() == TokenType.MAS || actual().getTipo() == TokenType.MENOS) {
            Token op = actual(); avanzar();
            NodoAST der  = parseExprMult();
            NodoAST nodo = new NodoAST("BinOp", op.getValor(), op.getLinea(), op.getColumna());
            nodo.agregarHijo(izq); nodo.agregarHijo(der);
            izq = nodo;
        }
        return izq;
    }

    private NodoAST parseExprMult() {
        NodoAST izq = parseExprUnaria();
        while (actual().getTipo() == TokenType.POR || actual().getTipo() == TokenType.DIVIDIR
                || actual().getTipo() == TokenType.MODULO) {
            Token op = actual(); avanzar();
            NodoAST der  = parseExprUnaria();
            NodoAST nodo = new NodoAST("BinOp", op.getValor(), op.getLinea(), op.getColumna());
            nodo.agregarHijo(izq); nodo.agregarHijo(der);
            izq = nodo;
        }
        return izq;
    }

    private NodoAST parseExprUnaria() {
        if (actual().getTipo() == TokenType.NO || actual().getTipo() == TokenType.MENOS) {
            Token op = actual(); avanzar();
            NodoAST nodo = new NodoAST("UnOp", op.getValor(), op.getLinea(), op.getColumna());
            nodo.agregarHijo(parseExprUnaria());
            return nodo;
        }
        return parsePrimaria();
    }

    private NodoAST parsePrimaria() {
        Token t = actual();
        return switch (t.getTipo()) {
            case ENTERO  -> { avanzar(); yield new NodoAST("Entero",   t.getValor(), t.getLinea(), t.getColumna()); }
            case DECIMAL -> { avanzar(); yield new NodoAST("Decimal",  t.getValor(), t.getLinea(), t.getColumna()); }
            case CADENA  -> { avanzar(); yield new NodoAST("Cadena",   t.getValor(), t.getLinea(), t.getColumna()); }
            case MERA    -> { avanzar(); yield new NodoAST("Bool",     "mera",       t.getLinea(), t.getColumna()); }
            case KAIRYU  -> { avanzar(); yield new NodoAST("Bool",     "kairyu",     t.getLinea(), t.getColumna()); }
            case ID      -> { avanzar(); yield new NodoAST("ID",       t.getValor(), t.getLinea(), t.getColumna()); }
            case PAREN_ABRE -> {
                avanzar();
                NodoAST expr = parseExpresion();
                if (!consumir(TokenType.PAREN_CIERRA))
                    errorSint("Se esperaba ')' para cerrar la expresión", actual());
                NodoAST grupo = new NodoAST("Grupo", "");
                grupo.agregarHijo(expr);
                yield grupo;
            }
            default -> {
                errorSint("Expresión no reconocida: '" + t.getValor() + "'", t);
                avanzar();
                yield new NodoAST("Error", t.getValor());
            }
        };
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private Token actual() {
        return pos < tokens.size() ? tokens.get(pos) : tokens.get(tokens.size()-1);
    }

    private boolean consumir(TokenType tipo) {
        if (actual().getTipo() == tipo) { avanzar(); return true; }
        return false;
    }

    private void avanzar() { if (pos < tokens.size() - 1) pos++; }

    private boolean esTipo(TokenType t) {
        return t == TokenType.YORU || t == TokenType.ZORO || t == TokenType.GOMU || t == TokenType.HAKI;
    }

    private boolean esRelacional(TokenType t) {
        return t == TokenType.MENOR || t == TokenType.MAYOR
                || t == TokenType.MENOR_IGUAL || t == TokenType.MAYOR_IGUAL;
    }

    private boolean esFinBloque() {
        TokenType t = actual().getTipo();
        return t == TokenType.LLAVE_CIERRA || t == TokenType.KAIZOKU;
    }

    private boolean esEOF() {
        return actual().getTipo() == TokenType.EOF;
    }

    private void errorSint(String desc, Token t) {
        errores.add(new ErrorEntry(TipoError.SINTACTICO, desc, t.getLinea(), t.getColumna(),
                "Token encontrado: '" + t.getValor() + "' (" + t.getTipo() + ")"));
    }

    private void actualizarValorSimbolo(String nombre, String val) {
        for (SimboloEntry s : simbolos) {
            if (s.getNombre().equals(nombre) && s.getAmbito().equals(ambitoActual)) {
                s.setValor(val); return;
            }
        }
    }
}
