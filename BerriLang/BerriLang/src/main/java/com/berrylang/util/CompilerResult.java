package com.berrylang.util;

import com.berrylang.model.ErrorEntry;
import com.berrylang.model.SimboloEntry;
import com.berrylang.model.Token;
import com.berrylang.parser.NodoAST;

import java.util.List;

/**
 * BerriLang - Resultado de una compilación completa
 */
public class CompilerResult {

    private final List<Token>       tokens;
    private final List<ErrorEntry>  errores;
    private final List<SimboloEntry> simbolos;
    private final NodoAST           arbol;
    private final boolean           exitoso;

    public CompilerResult(List<Token> tokens, List<ErrorEntry> errores,
                          List<SimboloEntry> simbolos, NodoAST arbol) {
        this.tokens   = tokens;
        this.errores  = errores;
        this.simbolos = simbolos;
        this.arbol    = arbol;
        this.exitoso  = errores.isEmpty();
    }

    public List<Token>       getTokens()   { return tokens; }
    public List<ErrorEntry>  getErrores()  { return errores; }
    public List<SimboloEntry> getSimbolos(){ return simbolos; }
    public NodoAST           getArbol()    { return arbol; }
    public boolean           isExitoso()  { return exitoso; }
}
