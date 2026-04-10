package com.berrylang.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * BerriLang - Nodo del Árbol de Sintaxis (AST)
 */
public class NodoAST {

    private final String tipo;
    private final String valor;
    private final List<NodoAST> hijos = new ArrayList<>();
    private final int linea;
    private final int columna;

    public NodoAST(String tipo, String valor, int linea, int columna) {
        this.tipo    = tipo;
        this.valor   = valor;
        this.linea   = linea;
        this.columna = columna;
    }

    public NodoAST(String tipo, String valor) {
        this(tipo, valor, 0, 0);
    }

    public void agregarHijo(NodoAST hijo) {
        if (hijo != null) hijos.add(hijo);
    }

    public String      getTipo()    { return tipo; }
    public String      getValor()   { return valor; }
    public List<NodoAST> getHijos() { return hijos; }
    public int         getLinea()   { return linea; }
    public int         getColumna() { return columna; }

    /**
     * Representación en texto del árbol completo (para consola)
     */
    public String toArbol(String prefijo, boolean ultimo) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefijo).append(ultimo ? "└── " : "├── ");
        sb.append(tipo).append(valor.isEmpty() ? "" : ": " + valor).append("\n");
        String nuevoPrefijo = prefijo + (ultimo ? "    " : "│   ");
        for (int i = 0; i < hijos.size(); i++) {
            sb.append(hijos.get(i).toArbol(nuevoPrefijo, i == hijos.size() - 1));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toArbol("", true);
    }
}
