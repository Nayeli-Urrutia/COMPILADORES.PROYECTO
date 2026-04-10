package com.berrylang.model;

/**
 * BerriLang - Representación de un Error (léxico o sintáctico)
 */
public class ErrorEntry {

    public enum TipoError { LEXICO, SINTACTICO }

    private final TipoError tipo;
    private final String descripcion;
    private final int linea;
    private final int columna;
    private final String causa;

    public ErrorEntry(TipoError tipo, String descripcion, int linea, int columna, String causa) {
        this.tipo        = tipo;
        this.descripcion = descripcion;
        this.linea       = linea;
        this.columna     = columna;
        this.causa       = causa;
    }

    public TipoError getTipo()       { return tipo; }
    public String    getDescripcion(){ return descripcion; }
    public int       getLinea()      { return linea; }
    public int       getColumna()    { return columna; }
    public String    getCausa()      { return causa; }

    @Override
    public String toString() {
        return String.format("[%s] L%d:C%d - %s (%s)", tipo, linea, columna, descripcion, causa);
    }
}
