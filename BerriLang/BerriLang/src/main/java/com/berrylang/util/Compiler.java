package com.berrylang.util;

import com.berrylang.lexer.Lexer;
import com.berrylang.model.ErrorEntry;
import com.berrylang.parser.NodoAST;
import com.berrylang.parser.Parser;

import java.util.ArrayList;
import java.util.List;

/**
 * BerriLang - Fachada del compilador
 * Une el analizador léxico y el sintáctico en un solo pipeline.
 */
public class Compiler {

    public static CompilerResult compilar(String codigoFuente) {
        // Fase 1: análisis léxico
        Lexer lexer = new Lexer(codigoFuente);
        lexer.analizar();

        // Fase 2: análisis sintáctico
        Parser parser = new Parser(lexer.getTokens());
        NodoAST arbol = null;
        List<ErrorEntry> erroresSint = new ArrayList<>();

        if (lexer.getErrores().isEmpty()) {
            arbol = parser.parsear();
            erroresSint = parser.getErrores();
        }

        // Combinar errores
        List<ErrorEntry> todosErrores = new ArrayList<>();
        todosErrores.addAll(lexer.getErrores());
        todosErrores.addAll(erroresSint);

        return new CompilerResult(
                lexer.getTokens(),
                todosErrores,
                parser.getSimbolos(),
                arbol
        );
    }
}
