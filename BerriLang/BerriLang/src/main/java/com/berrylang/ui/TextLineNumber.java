package com.berrylang.ui;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;

/**
 * BerriLang - Numeración de líneas para el editor de texto
 * Basado en el componente clásico de Rob Camick, adaptado al tema One Piece.
 */
public class TextLineNumber extends JPanel implements CaretListener, DocumentListener {

    private final JTextArea textArea;
    private int lastDigits = 0;
    private int lastLine   = 0;

    private static final int MIN_DIGITS  = 3;
    private static final int BORDER_GAP  = 8;
    private static final Color FG_NORMAL = new Color(0x555E6D);
    private static final Color FG_CURRENT= OnePieceTheme.ACCENT_GOLD;

    public TextLineNumber(JTextArea area) {
        this.textArea = area;
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        setBackground(new Color(0x161B22));
        setForeground(FG_NORMAL);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, OnePieceTheme.BORDER_DEFAULT));
        area.getDocument().addDocumentListener(this);
        area.addCaretListener(this);
    }

    @Override
    public Dimension getPreferredSize() {
        int lines = textArea.getLineCount();
        int digits = Math.max(MIN_DIGITS, String.valueOf(lines).length());
        if (digits != lastDigits) {
            lastDigits = digits;
            FontMetrics fm = getFontMetrics(getFont());
            int width = fm.charWidth('0') * digits + BORDER_GAP * 2;
            Dimension size = new Dimension(width, Integer.MAX_VALUE);
            setPreferredSize(size);
        }
        return super.getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        FontMetrics fm   = g.getFontMetrics();
        int         clip  = g.getClipBounds().height;
        int         rowH  = textArea.getFontMetrics(textArea.getFont()).getHeight();
        int         start = textArea.viewToModel2D(new Point(0, g.getClipBounds().y)) ;

        int currentLine = 1;
        try { currentLine = textArea.getLineOfOffset(textArea.getCaretPosition()) + 1; }
        catch (BadLocationException ignored) {}

        int line = 1;
        try { line = textArea.getLineOfOffset(start) + 1; }
        catch (BadLocationException ignored) {}

        int y = 0;
        try {
            Rectangle r = textArea.modelToView2D(start).getBounds();
            y = r.y - textArea.getVisibleRect().y + fm.getAscent();
        } catch (BadLocationException ignored) {}

        int totalLines = textArea.getLineCount();
        while (line <= totalLines && y - fm.getAscent() < clip) {
            g.setColor(line == currentLine ? FG_CURRENT : FG_NORMAL);
            String num = String.valueOf(line);
            int x = getWidth() - BORDER_GAP - fm.stringWidth(num);
            g.drawString(num, x, y);
            y += rowH;
            line++;
        }
    }

    // Actualizar al modificar el documento
    public void insertUpdate(DocumentEvent e)  { updateUI(); }
    public void removeUpdate(DocumentEvent e)  { updateUI(); }
    public void changedUpdate(DocumentEvent e) { updateUI(); }
    public void caretUpdate(CaretEvent e) {
        try {
            int newLine = textArea.getLineOfOffset(textArea.getCaretPosition()) + 1;
            if (newLine != lastLine) { lastLine = newLine; repaint(); }
        } catch (BadLocationException ignored) {}
    }

    public void updateUI() {
        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
        });
    }
}
