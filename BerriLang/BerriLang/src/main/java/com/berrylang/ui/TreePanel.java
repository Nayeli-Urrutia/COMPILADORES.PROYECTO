package com.berrylang.ui;

import com.berrylang.parser.NodoAST;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BerriLang - Panel de Árbol de Derivación
 *
 * Dibuja el árbol sintáctico igual al de la pizarra:
 *   - Los nodos NO terminales (internos) van en cajas rectangulares
 *     con bordes redondeados, estilo <sent_asig> <expresion> etc.
 *   - Los nodos hoja (terminales: literales, operadores, IDs)
 *     van como texto en cursiva sin caja, igual que en la pizarrón
 *   - Las conexiones son líneas rectas verticales/diagonales
 *     desde el centro-inferior del padre al centro-superior del hijo
 *
 * El árbol se calcula de forma recursiva:
 *   1. Se calcula el ancho mínimo de cada subárbol (post-order)
 *   2. Se asignan las posiciones X a cada nodo (in-order)
 *   3. Se dibuja en el JPanel con Graphics2D (pre-order)
 */
public class TreePanel extends JPanel {

    // ── Constantes de diseño ─────────────────────────────────────────────
    private static final int NODE_W      = 120;   // ancho base de cada caja
    private static final int NODE_H      = 30;    // alto de cada caja
    private static final int H_GAP       = 20;    // espacio horizontal entre nodos hermanos
    private static final int V_GAP       = 55;    // espacio vertical entre niveles
    private static final int MARGIN      = 30;    // margen exterior
    private static final int ARC         = 8;     // radio de esquinas redondeadas

    // ── Colores temáticos One Piece ──────────────────────────────────────
    private static final Color BOX_FILL    = new Color(0x1E2D40);  // azul marino oscuro
    private static final Color BOX_BORDER  = new Color(0x4A90D9);  // azul One Piece
    private static final Color BOX_TEXT    = new Color(0xA8C7F8);  // texto caja claro
    private static final Color LEAF_TEXT   = new Color(0xF5A623);  // dorado Luffy (terminales)
    private static final Color LINE_COLOR  = new Color(0x555E6D);  // líneas grises suaves
    private static final Color BG_COLOR    = new Color(0x1A1E27);  // fondo editor

    // ── Estado ───────────────────────────────────────────────────────────
    private NodoAST raiz = null;

    // Posición calculada de cada nodo: mapeamos nodo → Point (centro-superior)
    private final Map<NodoAST, Point> posiciones = new HashMap<>();
    private Dimension totalSize = new Dimension(800, 400);

    // ── Fuentes ──────────────────────────────────────────────────────────
    private final Font fuenteCaja  = new Font(Font.MONOSPACED, Font.PLAIN,  12);
    private final Font fuenteHoja  = new Font(Font.SANS_SERIF, Font.ITALIC, 13);

    public TreePanel() {
        setBackground(BG_COLOR);
    }

    // ── API pública ──────────────────────────────────────────────────────

    public void mostrar(NodoAST nuevaRaiz) {
        this.raiz = nuevaRaiz;
        posiciones.clear();
        if (raiz != null) {
            // Paso 1: calcular anchos de subárboles
            calcularAnchos(raiz);
            // Paso 2: asignar posiciones X e Y
            // nivel empieza en 0 para que la raíz quede pegada arriba
            int[] xCursor = { MARGIN };
            asignarPosiciones(raiz, xCursor, 0);
            // Paso 3: calcular tamaño total del canvas
            int maxX = posiciones.values().stream().mapToInt(p -> p.x).max().orElse(400) + NODE_W + MARGIN;
            int maxY = posiciones.values().stream().mapToInt(p -> p.y).max().orElse(200) + NODE_H + MARGIN;
            totalSize = new Dimension(maxX, maxY);
        }
        setPreferredSize(totalSize);
        revalidate();
        repaint();
    }

    public void limpiar() {
        raiz = null;
        posiciones.clear();
        repaint();
    }

    // ── Cálculo de layout ────────────────────────────────────────────────

    /**
     * Mapa auxiliar: ancho total (en píxeles) que ocupa el subárbol de cada nodo.
     */
    private final Map<NodoAST, Integer> anchos = new HashMap<>();

    /**
     * Calcula recursivamente el ancho mínimo del subárbol de cada nodo.
     * Un nodo hoja ocupa exactamente NODE_W.
     * Un nodo interno ocupa la suma de los anchos de sus hijos + gaps.
     */
    private int calcularAnchos(NodoAST nodo) {
        List<NodoAST> hijos = nodo.getHijos();
        if (hijos.isEmpty()) {
            // Nodo hoja: medir el texto real para que quepa
            int ancho = Math.max(NODE_W, etiqueta(nodo).length() * 8 + H_GAP);
            anchos.put(nodo, ancho);
            return ancho;
        }
        int total = 0;
        for (NodoAST hijo : hijos) {
            total += calcularAnchos(hijo);
        }
        // Agregar gaps entre hijos
        total += H_GAP * (hijos.size() - 1);
        // El nodo interno necesita al menos su propio ancho
        total = Math.max(total, nodeWidth(nodo));
        anchos.put(nodo, total);
        return total;
    }

    /**
     * Asigna la posición (x, y) a cada nodo.
     * xCursor[0] es la posición x actual del extremo izquierdo disponible.
     * El nivel determina la Y. nivel=0 → raíz pegada al tope con solo MARGIN.
     */
    private void asignarPosiciones(NodoAST nodo, int[] xCursor, int nivel) {
        List<NodoAST> hijos = nodo.getHijos();
        int anchoTotal = anchos.get(nodo);
        // Y calculado desde el margen superior, sin duplicar MARGIN
        int y = MARGIN + nivel * V_GAP;

        if (hijos.isEmpty()) {
            // Hoja: centrada en su ranura
            int cx = xCursor[0] + anchoTotal / 2;
            posiciones.put(nodo, new Point(cx, y));
            xCursor[0] += anchoTotal + H_GAP;
            return;
        }

        // Guardar xInicio antes de procesar hijos
        int xInicio = xCursor[0];

        // Procesar hijos primero (in-order para calcular sus centros)
        for (NodoAST hijo : hijos) {
            asignarPosiciones(hijo, xCursor, nivel + 1);
        }
        xCursor[0] -= H_GAP; // quitar el último gap sobrante

        // El padre se centra sobre el span de sus hijos
        Point primero = posiciones.get(hijos.get(0));
        Point ultimo  = posiciones.get(hijos.get(hijos.size() - 1));
        int cx = (primero.x + ultimo.x) / 2;
        posiciones.put(nodo, new Point(cx, y));

        // Avanzar xCursor al final del subárbol
        xCursor[0] += H_GAP;
    }

    // ── Dibujo ───────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (raiz == null) {
            dibujarVacio(g);
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        // Primero dibujamos todas las líneas (para que queden detrás de los nodos)
        dibujarLineas(g2, raiz);
        // Luego los nodos encima
        dibujarNodos(g2, raiz);
    }

    /**
     * Dibuja recursivamente las líneas padre → hijo.
     */
    private void dibujarLineas(Graphics2D g2, NodoAST nodo) {
        Point pPadre = posiciones.get(nodo);
        if (pPadre == null) return;

        g2.setColor(LINE_COLOR);
        g2.setStroke(new BasicStroke(1f));

        int padreH = esHoja(nodo) ? 0 : NODE_H;
        int xPadre = pPadre.x;
        int yPadre = pPadre.y + padreH;  // parte inferior del padre

        for (NodoAST hijo : nodo.getHijos()) {
            Point pHijo = posiciones.get(hijo);
            if (pHijo == null) continue;
            int yHijo = pHijo.y;  // parte superior del hijo
            // Línea recta padre → hijo
            g2.drawLine(xPadre, yPadre, pHijo.x, yHijo);
            // Continuar recursivo
            dibujarLineas(g2, hijo);
        }
    }

    /**
     * Dibuja recursivamente los nodos (cajas o texto).
     */
    private void dibujarNodos(Graphics2D g2, NodoAST nodo) {
        Point p = posiciones.get(nodo);
        if (p == null) return;

        String etiqueta = etiqueta(nodo);

        if (esHoja(nodo)) {
            // ── Terminal: texto en cursiva dorada, sin caja ──
            g2.setFont(fuenteHoja);
            g2.setColor(LEAF_TEXT);
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(etiqueta);
            g2.drawString(etiqueta, p.x - tw / 2, p.y + fm.getAscent() - 2);
        } else {
            // ── No terminal: caja redondeada con texto monoespacio ──
            g2.setFont(fuenteCaja);
            FontMetrics fm = g2.getFontMetrics();
            int w = Math.max(NODE_W, fm.stringWidth(etiqueta) + 20);
            int x = p.x - w / 2;
            int y = p.y;

            // Fondo de la caja
            g2.setColor(BOX_FILL);
            g2.fillRoundRect(x, y, w, NODE_H, ARC, ARC);
            // Borde de la caja
            g2.setColor(BOX_BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(x, y, w, NODE_H, ARC, ARC);
            // Texto centrado
            g2.setColor(BOX_TEXT);
            int tx = p.x - fm.stringWidth(etiqueta) / 2;
            int ty = y + (NODE_H + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(etiqueta, tx, ty);
        }

        // Recursión sobre hijos
        for (NodoAST hijo : nodo.getHijos()) {
            dibujarNodos(g2, hijo);
        }
    }

    private void dibujarVacio(Graphics g) {
        g.setColor(new Color(0x555E6D));
        g.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 14));
        String msg = "Compila un programa para ver el árbol de derivación";
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth()  - fm.stringWidth(msg)) / 2;
        int y = (getHeight() + fm.getAscent())       / 2;
        g.drawString(msg, x, y);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    /**
     * Etiqueta que se muestra en el nodo.
     * Para nodos internos: "<tipo>" o "<tipo>: valor"
     * Para hojas: solo el valor
     */
    private String etiqueta(NodoAST nodo) {
        String tipo  = nodo.getTipo();
        String valor = nodo.getValor();
        if (esHoja(nodo)) {
            // Hoja: mostrar solo el valor (el terminal real)
            return valor.isEmpty() ? tipo : valor;
        }
        // No terminal: mostrar en formato <tipo> igual a la pizarrón
        if (valor == null || valor.isEmpty()) {
            return "<" + tipo.toLowerCase() + ">";
        }
        // Con valor corto lo incluimos en la misma caja
        if (valor.length() <= 12) {
            return "<" + tipo.toLowerCase() + ": " + valor + ">";
        }
        // Valor largo: solo el tipo
        return "<" + tipo.toLowerCase() + ">";
    }

    /**
     * Un nodo es hoja si no tiene hijos O si es uno de los tipos terminales
     * que vienen del parser (literales, identificadores simples).
     */
    private boolean esHoja(NodoAST nodo) {
        if (nodo.getHijos().isEmpty()) return true;
        // Nodos que aunque técnicamente tienen hijos, los tratamos
        // como terminales visuales según el estilo de la pizarrón
        String tipo = nodo.getTipo();
        return tipo.equals("Entero")   ||
                tipo.equals("Decimal")  ||
                tipo.equals("Cadena")   ||
                tipo.equals("Bool")     ||
                tipo.equals("ID")       ||
                tipo.equals("Error");
    }

    /**
     * Ancho visual de la caja de un nodo no-terminal.
     */
    private int nodeWidth(NodoAST nodo) {
        // Usar FontMetrics real si es posible; si no, estimación
        String et = etiqueta(nodo);
        return Math.max(NODE_W, et.length() * 8 + 20);
    }
}