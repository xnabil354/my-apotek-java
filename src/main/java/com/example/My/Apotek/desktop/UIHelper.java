package com.example.My.Apotek.desktop;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;

public class UIHelper {
    public static final Color BG = new Color(246, 248, 252);
    public static final Color CARD = Color.WHITE;
    public static final Color PRIMARY = new Color(79, 70, 229);
    public static final Color PRIMARY_LIGHT = new Color(238, 242, 255);
    public static final Color SUCCESS = new Color(16, 185, 129);
    public static final Color DANGER = new Color(239, 68, 68);
    public static final Color WARNING = new Color(245, 158, 11);
    public static final Color TEXT = new Color(30, 41, 59);
    public static final Color TEXT_MUTED = new Color(100, 116, 139);
    public static final Color SIDEBAR_BG = new Color(15, 23, 42);
    public static final Color SIDEBAR_HOVER = new Color(30, 41, 59);
    public static final Color BORDER = new Color(226, 232, 240);
    public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font MONO = new Font("Consolas", Font.PLAIN, 13);

    public static JPanel card(String title) {
        JPanel p = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setLayout(new BorderLayout(12, 12));
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        if (title != null) {
            JLabel lbl = new JLabel(title);
            lbl.setFont(SUBTITLE);
            lbl.setForeground(TEXT);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
            p.add(lbl, BorderLayout.NORTH);
        }
        return p;
    }

    public static JTextField styledField(String placeholder) {
        JTextField tf = new JTextField() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(BODY);
                    Insets ins = getInsets();
                    g2.drawString(placeholder, ins.left + 4, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        tf.setFont(BODY);
        tf.setForeground(TEXT);
        tf.setBackground(new Color(248, 250, 252));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        tf.setPreferredSize(new Dimension(200, 40));
        return tf;
    }

    public static JButton styledBtn(String text, Color bg) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 42));
        return btn;
    }

    public static void styleTable(JTable table) {
        table.setFont(BODY);
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(CARD);
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(PRIMARY);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(TEXT_MUTED);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER));
        header.setPreferredSize(new Dimension(0, 44));
        header.setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row,
                    int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                lbl.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
                if (!sel)
                    lbl.setBackground(row % 2 == 0 ? CARD : new Color(248, 250, 252));
                lbl.setForeground(sel ? PRIMARY : TEXT);
                return lbl;
            }
        });
    }

    public static JScrollPane styledScroll(JTable table) {
        styleTable(table);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(BORDER, 1, true));
        sp.getViewport().setBackground(CARD);
        return sp;
    }

    public static JPanel formRow(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(120, 30));
        row.add(lbl, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        return row;
    }

    public static JPanel statCard(String title, String value, Color accent) {
        JPanel p = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 5, getHeight(), 8, 8);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        JLabel t = new JLabel(title);
        t.setFont(SMALL);
        t.setForeground(TEXT_MUTED);
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 22));
        v.setForeground(accent);
        p.add(t);
        p.add(Box.createVerticalStrut(4));
        p.add(v);
        return p;
    }

    public static JButton createSidebarButton(String text, String type, boolean isActive) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isActive = Boolean.TRUE.equals(getClientProperty("Active"));

                if (isActive || getModel().isRollover()) {
                    g2.setColor(isActive ? PRIMARY : SIDEBAR_HOVER);
                    g2.fillRoundRect(12, 0, getWidth() - 24, getHeight(), 12, 12);
                }

                Color fg = isActive ? Color.WHITE : new Color(148, 163, 184);
                g2.setColor(fg);

                int ix = 32, iy = getHeight() / 2;
                Stroke thick = new BasicStroke(2f);
                g2.setStroke(thick);

                switch (type) {
                    case "inv": // Box
                        g2.drawRect(ix, iy - 6, 14, 12);
                        g2.drawLine(ix, iy - 6, ix + 7, iy);
                        g2.drawLine(ix + 14, iy - 6, ix + 7, iy);
                        break;
                    case "rx": // Pill
                        g2.rotate(Math.toRadians(45), ix + 7, iy);
                        g2.drawRoundRect(ix, iy - 4, 14, 8, 8, 8);
                        g2.drawLine(ix + 7, iy - 4, ix + 7, iy + 4);
                        g2.rotate(-Math.toRadians(45), ix + 7, iy);
                        break;
                    case "opn": // Clipboard
                        g2.drawRect(ix + 2, iy - 6, 10, 13);
                        g2.drawLine(ix + 4, iy - 2, ix + 10, iy - 2);
                        g2.drawLine(ix + 4, iy + 2, ix + 10, iy + 2);
                        break;
                    case "rep": // Chart
                        g2.drawLine(ix, iy + 6, ix, iy + 2);
                        g2.drawLine(ix + 5, iy + 6, ix + 5, iy - 4);
                        g2.drawLine(ix + 10, iy + 6, ix + 10, iy);
                        g2.drawLine(ix - 2, iy + 7, ix + 14, iy + 7);
                        break;
                    case "pos": // Cart
                        g2.drawRect(ix, iy - 4, 12, 8);
                        g2.fillOval(ix + 2, iy + 6, 3, 3);
                        g2.fillOval(ix + 9, iy + 6, 3, 3);
                        g2.drawLine(ix + 3, iy - 4, ix + 3, iy - 7);
                        g2.drawLine(ix + 9, iy - 4, ix + 9, iy - 7);
                        g2.drawLine(ix + 3, iy - 7, ix + 9, iy - 7);
                        break;
                    case "mims": // Book
                        g2.drawRect(ix + 2, iy - 6, 10, 12);
                        g2.drawLine(ix + 4, iy - 4, ix + 10, iy - 4);
                        g2.drawLine(ix + 4, iy, ix + 10, iy);
                        g2.drawLine(ix + 4, iy + 4, ix + 10, iy + 4);
                        break;
                    default:
                        g2.fillOval(ix + 4, iy - 3, 6, 6);
                }

                g2.setFont(new Font("Segoe UI", isActive ? Font.BOLD : Font.PLAIN, 14));
                g2.drawString(getText(), 64, iy + 5);

                g2.dispose();
            }
        };
        btn.putClientProperty("Active", isActive);
        btn.setPreferredSize(new Dimension(240, 48));
        btn.setMaximumSize(new Dimension(240, 48));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }
}
