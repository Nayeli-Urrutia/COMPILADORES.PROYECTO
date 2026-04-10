package com.berrylang.model;

/**
 * BerriLang - Entrada en la tabla de símbolos
 */
public class SimboloEntry {

    private final String nombre;
    private final String tipo;
    private String valor;
    private final int linea;
    private final int columna;
    private final String ambito;

    public SimboloEntry(String nombre, String tipo, String valor, int linea, int columna, String ambito) {
        this.nombre  = nombre;
        this.tipo    = tipo;
        this.valor   = valor;
        this.linea   = linea;
        this.columna = columna;
        this.ambito  = ambito;
    }

    public String getNombre()  { return nombre; }
    public String getTipo()    { return tipo; }
    public String getValor()   { return valor; }
    public int    getLinea()   { return linea; }
    public int    getColumna() { return columna; }
    public String getAmbito()  { return ambito; }

    public void setValor(String valor) { this.valor = valor; }
}
