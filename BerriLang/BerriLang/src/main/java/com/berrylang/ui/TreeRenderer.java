package com.berrylang.ui;

import com.berrylang.parser.NodoAST;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

/**
 * BerriLang - Panel de árbol sintáctico visual
 * Renderiza el AST como JTree con colores temáticos One Piece
 */
public class TreeRenderer extends JPanel {

    private final JTree tree;

    public TreeRenderer() {
        setLayout(new BorderLayout());
        setBackground(OnePieceTheme.BG_PANEL);
        tree = new JTree(new DefaultMutableTreeNode("Sin compilar"));
        tree.setBackground(OnePieceTheme.BG_PANEL);
        tree.setForeground(OnePieceTheme.TEXT_PRIMARY);
        tree.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        tree.setRowHeight(22);
        tree.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        tree.setCellRenderer(new ASTCellRenderer());

        JScrollPane scroll = new JScrollPane(tree);
        scroll.setBorder(BorderFactory.createLineBorder(OnePieceTheme.ACCENT_GOLD, 1));
        scroll.setBackground(OnePieceTheme.BG_PANEL);
        add(scroll, BorderLayout.CENTER);
    }

    public void mostrar(NodoAST raiz) {
        DefaultMutableTreeNode treeRoot = construirNodo(raiz);
        tree.setModel(new DefaultTreeModel(treeRoot));
        expandirTodo();
    }

    private DefaultMutableTreeNode construirNodo(NodoAST nodo) {
        String etiqueta = nodo.getTipo() + (nodo.getValor().isEmpty() ? "" : ": " + nodo.getValor());
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(etiqueta);
        for (NodoAST hijo : nodo.getHijos()) {
            treeNode.add(construirNodo(hijo));
        }
        return treeNode;
    }

    private void expandirTodo() {
        for (int i = 0; i < tree.getRowCount(); i++) tree.expandRow(i);
    }

    public void limpiar() {
        tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Sin compilar")));
    }

    // ── Renderer de celdas del árbol ─────────────────────────────────────
    static class ASTCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            String text = value.toString();

            setBackground(OnePieceTheme.BG_PANEL);
            setBackgroundNonSelectionColor(OnePieceTheme.BG_PANEL);
            setBackgroundSelectionColor(OnePieceTheme.ACCENT_GOLD.darker());

            if (text.startsWith("Programa"))       setForeground(OnePieceTheme.ACCENT_GOLD);
            else if (text.startsWith("Funcion"))   setForeground(new Color(0x61AFEF));
            else if (text.startsWith("Si")
                  || text.startsWith("Sino"))      setForeground(new Color(0xE5C07B));
            else if (text.startsWith("Mientras")
                  || text.startsWith("Para"))      setForeground(new Color(0xC678DD));
            else if (text.startsWith("Imprimir")) setForeground(new Color(0x98C379));
            else if (text.startsWith("Declaracion")) setForeground(new Color(0x56B6C2));
            else if (text.startsWith("BinOp")
                  || text.startsWith("UnOp"))      setForeground(new Color(0xE06C75));
            else if (text.startsWith("Entero")
                  || text.startsWith("Decimal")
                  || text.startsWith("Cadena")
                  || text.startsWith("Bool"))      setForeground(new Color(0xD19A66));
            else if (text.startsWith("ID"))        setForeground(OnePieceTheme.TEXT_PRIMARY);
            else                                   setForeground(OnePieceTheme.TEXT_SECONDARY);

            setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            setIcon(null);
            return this;
        }
    }
}
