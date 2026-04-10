package com.berrylang.ui;

import com.berrylang.model.ErrorEntry;
import com.berrylang.model.SimboloEntry;
import com.berrylang.model.Token;
import com.berrylang.util.Compiler;
import com.berrylang.util.CompilerResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;

/**
 * BerriLang - Ventana Principal
 * Interfaz gráfica completa con temática One Piece
 */
public class MainWindow extends JFrame {

    // ── Componentes principales ──────────────────────────────────────────
    private JTextArea      editorArea;
    private JTextArea      consoleArea;
    private JTable         tablaTokens;
    private JTable         tablaErrores;
    private JTable         tablaSimbolos;
    private TreeRenderer   treePanel;
    private JLabel         statusLabel;
    private JTabbedPane    bottomTabs;

    // Modelos de tabla
    private DefaultTableModel modelTokens;
    private DefaultTableModel modelErrores;
    private DefaultTableModel modelSimbolos;

    public MainWindow() {
        super("🏴‍☠️  BerriLang IDE  —  One Piece Mini-Compilador");
        configurarVentana();
        construirUI();
        cargarEjemplo();
    }

    // ── Configuración de ventana ─────────────────────────────────────────

    private void configurarVentana() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        // Icono
        try {
            Image icon = Toolkit.getDefaultToolkit()
                    .createImage(getClass().getResource("/icon.png"));
            setIconImage(icon);
        } catch (Exception ignored) {}
        getContentPane().setBackground(OnePieceTheme.BG_DARK);
    }

    // ── Construcción de la UI ────────────────────────────────────────────

    private void construirUI() {
        setLayout(new BorderLayout());
        add(crearToolbar(),      BorderLayout.NORTH);
        add(crearPanelCentral(), BorderLayout.CENTER);
        add(crearStatusBar(),    BorderLayout.SOUTH);
    }

    // ── Toolbar ──────────────────────────────────────────────────────────

    private JPanel crearToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        bar.setBackground(OnePieceTheme.BG_TOOLBAR);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, OnePieceTheme.ACCENT_GOLD));

        // Logo
        JLabel logo = new JLabel("🏴‍☠️  BerriLang");
        logo.setForeground(OnePieceTheme.ACCENT_GOLD);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logo.setBorder(new EmptyBorder(0, 8, 0, 16));
        bar.add(logo);

        bar.add(crearBoton("▶  Compilar",   OnePieceTheme.ACCENT_GREEN, e -> compilar()));
        bar.add(crearBoton("🗑  Limpiar",    OnePieceTheme.ACCENT_BLUE,  e -> limpiar()));
        bar.add(crearBoton("📂  Abrir",     OnePieceTheme.ACCENT_GOLD,  e -> abrirArchivo()));
        bar.add(crearBoton("💾  Guardar",   OnePieceTheme.ACCENT_GOLD,  e -> guardarArchivo()));
        bar.add(crearBoton("📋  Ejemplo",   OnePieceTheme.TEXT_SECONDARY, e -> cargarEjemplo()));

        return bar;
    }

    private JButton crearBoton(String texto, Color color, ActionListener al) {
        JButton btn = new JButton(texto);
        btn.setBackground(OnePieceTheme.BG_PANEL);
        btn.setForeground(color);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1, true),
                new EmptyBorder(4, 12, 4, 12)));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(al);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e)  { btn.setBackground(color.darker().darker()); }
            public void mouseExited(MouseEvent e)   { btn.setBackground(OnePieceTheme.BG_PANEL); }
        });
        return btn;
    }

    // ── Panel central (editor + resultados) ──────────────────────────────

    private JSplitPane crearPanelCentral() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                crearPanelEditor(), crearPanelResultados());
        split.setDividerLocation(380);
        split.setDividerSize(5);
        split.setBackground(OnePieceTheme.BG_DARK);
        split.setBorder(null);
        return split;
    }

    // ── Editor ───────────────────────────────────────────────────────────

    private JPanel crearPanelEditor() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(OnePieceTheme.BG_PANEL);
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, OnePieceTheme.BORDER_DEFAULT));

        // Header del editor
        JPanel header = crearHeader("✏️  Editor de código BerriLang", OnePieceTheme.ACCENT_GOLD);
        p.add(header, BorderLayout.NORTH);

        editorArea = new JTextArea();
        editorArea.setBackground(OnePieceTheme.BG_EDITOR);
        editorArea.setForeground(OnePieceTheme.TEXT_PRIMARY);
        editorArea.setCaretColor(OnePieceTheme.ACCENT_GOLD);
        editorArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        editorArea.setTabSize(4);
        editorArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Atajo Ctrl+Enter para compilar
        editorArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                InputEvent.CTRL_DOWN_MASK), "compilar");
        editorArea.getActionMap().put("compilar", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { compilar(); }
        });

        JScrollPane scroll = new JScrollPane(editorArea);
        scroll.setBorder(null);
        scroll.setBackground(OnePieceTheme.BG_EDITOR);

        // Números de línea
        TextLineNumber lineNum = new TextLineNumber(editorArea);
        scroll.setRowHeaderView(lineNum);

        p.add(scroll, BorderLayout.CENTER);

        // Hint
        JLabel hint = new JLabel("  Ctrl+Enter para compilar rápido");
        hint.setForeground(OnePieceTheme.TEXT_SECONDARY);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setBorder(new EmptyBorder(2, 4, 2, 4));
        hint.setBackground(OnePieceTheme.BG_TOOLBAR);
        hint.setOpaque(true);
        p.add(hint, BorderLayout.SOUTH);

        return p;
    }

    // ── Panel de resultados (tabs) ────────────────────────────────────────

    private JPanel crearPanelResultados() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(OnePieceTheme.BG_PANEL);

        bottomTabs = new JTabbedPane();
        bottomTabs.setBackground(OnePieceTheme.BG_PANEL);
        bottomTabs.setForeground(OnePieceTheme.TEXT_PRIMARY);
        bottomTabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Tab 1: Consola
        bottomTabs.addTab("🖥️ Consola",   crearTabConsola());
        // Tab 2: Tokens
        bottomTabs.addTab("🔤 Tokens",    crearTabTabla("Tokens", crearModeloTokens()));
        // Tab 3: Errores
        bottomTabs.addTab("❌ Errores",   crearTabTabla("Errores", crearModeloErrores()));
        // Tab 4: Símbolos
        bottomTabs.addTab("📦 Símbolos",  crearTabTabla("Simbolos", crearModeloSimbolos()));
        // Tab 5: Árbol
        treePanel = new TreeRenderer();
        bottomTabs.addTab("🌳 Árbol AST", treePanel);

        p.add(bottomTabs, BorderLayout.CENTER);
        return p;
    }

    private JPanel crearTabConsola() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(OnePieceTheme.BG_PANEL);
        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setBackground(new Color(0x0D1117));
        consoleArea.setForeground(OnePieceTheme.TEXT_SUCCESS);
        consoleArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        consoleArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane scroll = new JScrollPane(consoleArea);
        scroll.setBorder(null);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private JPanel crearTabTabla(String nombre, DefaultTableModel modelo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(OnePieceTheme.BG_PANEL);

        JTable tabla;
        if (nombre.equals("Tokens"))   { tablaTokens  = crearTabla(modelo); tabla = tablaTokens;   }
        else if (nombre.equals("Errores")) { tablaErrores = crearTabla(modelo); tabla = tablaErrores; }
        else                           { tablaSimbolos= crearTabla(modelo); tabla = tablaSimbolos; }

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(OnePieceTheme.BORDER_DEFAULT));
        scroll.setBackground(OnePieceTheme.BG_PANEL);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private JTable crearTabla(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setBackground(OnePieceTheme.BG_PANEL);
        t.setForeground(OnePieceTheme.TEXT_PRIMARY);
        t.setGridColor(OnePieceTheme.BORDER_DEFAULT);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(24);
        t.setSelectionBackground(OnePieceTheme.ACCENT_GOLD.darker().darker());
        t.setSelectionForeground(Color.WHITE);
        t.getTableHeader().setBackground(OnePieceTheme.BG_TOOLBAR);
        t.getTableHeader().setForeground(OnePieceTheme.ACCENT_GOLD);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setFillsViewportHeight(true);
        return t;
    }

    // ── Modelos de tabla ─────────────────────────────────────────────────

    private DefaultTableModel crearModeloTokens() {
        modelTokens = new DefaultTableModel(
                new String[]{"#", "Token", "Tipo", "Línea", "Columna"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        return modelTokens;
    }

    private DefaultTableModel crearModeloErrores() {
        modelErrores = new DefaultTableModel(
                new String[]{"#", "Tipo", "Descripción", "Línea", "Columna", "Causa"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        return modelErrores;
    }

    private DefaultTableModel crearModeloSimbolos() {
        modelSimbolos = new DefaultTableModel(
                new String[]{"#", "Nombre", "Tipo", "Valor", "Línea", "Columna", "Ámbito"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        return modelSimbolos;
    }

    // ── Status bar ───────────────────────────────────────────────────────

    private JPanel crearStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(OnePieceTheme.BG_TOOLBAR);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, OnePieceTheme.BORDER_DEFAULT));
        statusLabel = new JLabel("  Listo. Escribe tu código en BerriLang y presiona Compilar.");
        statusLabel.setForeground(OnePieceTheme.TEXT_SECONDARY);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setBorder(new EmptyBorder(3, 8, 3, 8));
        bar.add(statusLabel, BorderLayout.WEST);

        JLabel ver = new JLabel("BerriLang v1.0  🏴‍☠️  UMG Jutiapa 2026  ");
        ver.setForeground(OnePieceTheme.TEXT_SECONDARY);
        ver.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        bar.add(ver, BorderLayout.EAST);
        return bar;
    }

    // ── Header helper ─────────────────────────────────────────────────────

    private JPanel crearHeader(String titulo, Color color) {
        JPanel h = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        h.setBackground(OnePieceTheme.BG_TOOLBAR);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, OnePieceTheme.BORDER_DEFAULT));
        JLabel lbl = new JLabel(titulo);
        lbl.setForeground(color);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.add(lbl);
        return h;
    }

    // ── Acciones ─────────────────────────────────────────────────────────

    private void compilar() {
        String codigo = editorArea.getText().trim();
        if (codigo.isEmpty()) {
            setStatus("⚠️  El editor está vacío.", OnePieceTheme.TEXT_WARNING);
            return;
        }

        limpiarTablas();
        consoleArea.setText("");

        CompilerResult result = Compiler.compilar(codigo);

        // Llenar tabla de tokens
        int i = 1;
        for (Token t : result.getTokens()) {
            if (t.getTipo() != Token.TokenType.EOF) {
                modelTokens.addRow(new Object[]{
                        i++, t.getValor(), t.getTipo().name(), t.getLinea(), t.getColumna()
                });
            }
        }

        // Llenar tabla de errores
        int j = 1;
        for (ErrorEntry e : result.getErrores()) {
            modelErrores.addRow(new Object[]{
                    j++, e.getTipo().name(), e.getDescripcion(),
                    e.getLinea(), e.getColumna(), e.getCausa()
            });
        }

        // Llenar tabla de símbolos
        int k = 1;
        for (SimboloEntry s : result.getSimbolos()) {
            modelSimbolos.addRow(new Object[]{
                    k++, s.getNombre(), s.getTipo(), s.getValor(),
                    s.getLinea(), s.getColumna(), s.getAmbito()
            });
        }

        // Árbol AST
        if (result.getArbol() != null) {
            treePanel.mostrar(result.getArbol());
        } else {
            treePanel.limpiar();
        }

        // Consola
        if (result.isExitoso()) {
            consoleArea.setForeground(OnePieceTheme.TEXT_SUCCESS);
            consoleArea.setText(
                "╔══════════════════════════════════════╗\n" +
                "║  ✅  Compilación exitosa               ║\n" +
                "╚══════════════════════════════════════╝\n\n" +
                "  🏴‍☠️  ¡King of the Pirates! El código es válido.\n\n" +
                "  Tokens encontrados : " + (result.getTokens().size()-1) + "\n" +
                "  Símbolos definidos : " + result.getSimbolos().size() + "\n" +
                "  Errores            : 0\n\n" +
                result.getArbol().toString()
            );
            setStatus("✅  Compilación exitosa — " + (result.getTokens().size()-1) + " tokens, " +
                    result.getSimbolos().size() + " símbolos", OnePieceTheme.TEXT_SUCCESS);
            bottomTabs.setSelectedIndex(0);
        } else {
            consoleArea.setForeground(OnePieceTheme.TEXT_ERROR);
            StringBuilder sb = new StringBuilder();
            sb.append("╔══════════════════════════════════════╗\n");
            sb.append("║  ❌  Compilación con errores            ║\n");
            sb.append("╚══════════════════════════════════════╝\n\n");
            for (ErrorEntry e : result.getErrores()) {
                sb.append("  [").append(e.getTipo()).append("] Línea ")
                  .append(e.getLinea()).append(", Col ").append(e.getColumna())
                  .append(" → ").append(e.getDescripcion()).append("\n")
                  .append("         Causa: ").append(e.getCausa()).append("\n\n");
            }
            consoleArea.setText(sb.toString());
            setStatus("❌  " + result.getErrores().size() + " error(es) encontrado(s)", OnePieceTheme.TEXT_ERROR);
            bottomTabs.setSelectedIndex(2); // ir a errores
        }
    }

    private void limpiar() {
        editorArea.setText("");
        limpiarTablas();
        consoleArea.setText("");
        treePanel.limpiar();
        setStatus("Listo.", OnePieceTheme.TEXT_SECONDARY);
    }

    private void limpiarTablas() {
        modelTokens.setRowCount(0);
        modelErrores.setRowCount(0);
        modelSimbolos.setRowCount(0);
    }

    private void abrirArchivo() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Abrir archivo BerriLang (.berry)");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String content = Files.readString(fc.getSelectedFile().toPath());
                editorArea.setText(content);
                setStatus("📂 Archivo cargado: " + fc.getSelectedFile().getName(),
                        OnePieceTheme.TEXT_SECONDARY);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al abrir el archivo:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void guardarArchivo() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar archivo BerriLang (.berry)");
        fc.setSelectedFile(new File("programa.berry"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Files.writeString(fc.getSelectedFile().toPath(), editorArea.getText());
                setStatus("💾 Guardado: " + fc.getSelectedFile().getName(),
                        OnePieceTheme.TEXT_SUCCESS);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setStatus(String msg, Color color) {
        statusLabel.setText("  " + msg);
        statusLabel.setForeground(color);
    }

    // ── Ejemplo de código BerriLang ──────────────────────────────────────

    private void cargarEjemplo() {
        editorArea.setText(
                "// ====================================================\n" +
                        "// PRUEBA  - Válida: Múltiples tipos y booleano\n" +
                        "// ====================================================\n" +
                        "nakama Prueba{\n" +
                        "    gomu nombre = \"Roronoa Zoro\";\n" +
                        "    zoro altura = 1.81;\n" +
                        "    haki esPirata = mera;\n" +
                        "    nami(nombre);\n" +
                        "    nami(altura);\n" +
                        "    nami(esPirata);\n" +
                        "}\n" +
                        "kaizoku\n"
        );

        setStatus("📋  Ejemplo cargado. Presiona ▶ Compilar.", OnePieceTheme.TEXT_SECONDARY);
    }

    // ── Entrada principal ────────────────────────────────────────────────

    public static void main(String[] args) {
        // Aplicar FlatLaf Dark si está disponible
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
        } catch (Exception e) {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
        }

        // Colores base FlatLaf
        UIManager.put("TabbedPane.background", OnePieceTheme.BG_PANEL);
        UIManager.put("TabbedPane.foreground", OnePieceTheme.TEXT_PRIMARY);
        UIManager.put("TabbedPane.selectedBackground", OnePieceTheme.BG_DARK);
        UIManager.put("SplitPane.dividerSize", 5);

        SwingUtilities.invokeLater(() -> {
            MainWindow win = new MainWindow();
            win.setVisible(true);
        });
    }
}
