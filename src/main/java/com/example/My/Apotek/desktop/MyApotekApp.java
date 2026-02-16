package com.example.My.Apotek.desktop;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.apache.poi.xssf.usermodel.*;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.formdev.flatlaf.FlatLightLaf;
import static com.example.My.Apotek.desktop.UIHelper.*;

public class MyApotekApp extends JFrame {
    private static final String DB = "jdbc:h2:./data/apotek_db;AUTO_SERVER=TRUE";
    private static final String U = "sa", P = "";
    private JPanel contentPanel;
    private CardLayout cardLayout;

    public MyApotekApp() {
        setTitle("My Apotek Professional");
        setSize(1280, 860);
        setMinimumSize(new Dimension(1000, 700));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        ensureTableExists();

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(createSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG);
        contentPanel.add(createInventoryPanel(), "inv");
        contentPanel.add(createPrescriptionPanel(), "rx");
        contentPanel.add(createOpnamePanel(), "opn");
        contentPanel.add(createReportsPanel(), "rep");
        contentPanel.add(createPenjualanPanel(), "pos");
        contentPanel.add(createMIMSPanel(), "mims");
        root.add(contentPanel, BorderLayout.CENTER);
        setContentPane(root);
    }

    private java.util.List<JButton> sidebarButtons = new java.util.ArrayList<>();

    private JPanel createSidebar() {
        JPanel sb = new JPanel();
        sb.setBackground(SIDEBAR_BG);
        sb.setPreferredSize(new Dimension(260, 0));
        sb.setLayout(new BorderLayout());

        JPanel brand = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(PRIMARY);
                g2.fillRoundRect(24, 30, 36, 36, 12, 12);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.5f));
                g2.rotate(Math.toRadians(45), 42, 48);
                g2.drawRoundRect(36, 44, 12, 8, 4, 4);
                g2.drawLine(42, 44, 42, 52);
                g2.rotate(-Math.toRadians(45), 42, 48);
            }
        };
        brand.setBackground(SIDEBAR_BG);
        brand.setPreferredSize(new Dimension(260, 100));
        brand.setLayout(null);

        JLabel brandName = new JLabel("My Apotek");
        brandName.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brandName.setForeground(Color.WHITE);
        brandName.setBounds(72, 28, 160, 30);
        brand.add(brandName);

        JLabel brandSub = new JLabel("Professional");
        brandSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        brandSub.setForeground(new Color(148, 163, 184));
        brandSub.setBounds(74, 56, 160, 20);
        brand.add(brandSub);

        sb.add(brand, BorderLayout.NORTH);

        JPanel menu = new JPanel();
        menu.setBackground(SIDEBAR_BG);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));

        String[][] items = {
                { "Inventory", "inv", "Manage stock & items" },
                { "Resep Dokter", "rx", "Process prescriptions" },
                { "Stok Opname", "opn", "Audit & adjustments" },
                { "Laporan", "rep", "Financial reports" },
                { "Penjualan", "pos", "Direct sales (OTC)" },
                { "MIMS Apotek", "mims", "Drug database" }
        };

        for (String[] it : items) {
            String label = it[0];
            String type = it[1];
            boolean isActive = type.equals("inv");

            JButton btn = createSidebarButton(label, type, isActive);

            btn.addActionListener(e -> {
                cardLayout.show(contentPanel, type);
                updateSidebarState(btn);
            });

            sidebarButtons.add(btn);
            menu.add(Box.createVerticalStrut(4));
            menu.add(btn);
        }
        menu.add(Box.createVerticalStrut(4));

        JPanel menuWrapper = new JPanel(new BorderLayout());
        menuWrapper.setBackground(SIDEBAR_BG);
        menuWrapper.add(menu, BorderLayout.NORTH);

        JLabel ver = new JLabel("v2.1 Pro  \u00A9 2026", SwingConstants.CENTER);
        ver.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        ver.setForeground(new Color(51, 65, 85));
        ver.setPreferredSize(new Dimension(260, 40));
        menuWrapper.add(ver, BorderLayout.SOUTH);

        sb.add(menuWrapper, BorderLayout.CENTER);
        return sb;
    }

    private void updateSidebarState(JButton active) {
        for (JButton btn : sidebarButtons) {
            btn.putClientProperty("Active", btn == active);
            btn.repaint();
        }
    }

    private JPanel createInventoryPanel() {
        JPanel p = new JPanel(new BorderLayout(16, 16));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel header = new JLabel("Manajemen Inventory Obat");
        header.setFont(TITLE);
        header.setForeground(TEXT);

        JPanel formCard = card("Input Obat Baru");
        JPanel formGrid = new JPanel(new GridLayout(7, 2, 12, 8));
        formGrid.setOpaque(false);
        String[] labels = { "Nama Obat", "Qty", "No Faktur", "Harga Beli", "Batch", "H.Beli+PPN 11%", "PBF",
                "H.Jual Apotek", "Supplier", "H.Plot", "Satuan", "Indikasi", "Golongan", "" };
        JTextField[] f = new JTextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            if (labels[i].isEmpty()) {
                formGrid.add(new JLabel());
                continue;
            }
            f[i] = styledField(labels[i]);
            if (i == 5) {
                f[i].setEditable(false);
                f[i].setBackground(new Color(241, 245, 249));
            }
            formGrid.add(formRow(labels[i] + ":", f[i]));
        }
        f[3].addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                try {
                    f[5].setText(String.format("%.0f", Double.parseDouble(f[3].getText()) * 1.11));
                } catch (Exception ignored) {
                    f[5].setText("");
                }
            }
        });
        formCard.add(formGrid, BorderLayout.CENTER);

        JButton btnAdd = styledBtn("Tambah Obat", PRIMARY);
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrap.setOpaque(false);
        btnWrap.add(btnAdd);
        formCard.add(btnWrap, BorderLayout.SOUTH);

        DefaultTableModel model = new DefaultTableModel(new String[] { "No Faktur", "Nama", "Batch", "PBF", "Satuan",
                "Golongan", "Qty", "H.Beli", "H.Beli+PPN", "H.Jual", "H.Plot" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(model);

        btnAdd.addActionListener(e -> {
            try (Connection c = DriverManager.getConnection(DB, U, P)) {
                double hb = parseNum(f[3]), ppn = hb * 1.11, hj = parseNum(f[7]), hp = parseNum(f[9]);
                int qty = (int) parseNum(f[1]);
                PreparedStatement ps = c.prepareStatement(
                        "MERGE INTO obat(no_faktur, nama_obat, nomor_batch, nama_pbf, supplier, satuan, golongan, quantity, harga_beli, harga_beli_ppn, harga_jual_apotek, harga_plot, indikasi) KEY(no_faktur) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)");
                ps.setString(1, f[2].getText());
                ps.setString(2, f[0].getText());
                ps.setString(3, f[4].getText());
                ps.setString(4, f[6].getText());
                ps.setString(5, f[8].getText());
                ps.setString(6, f[10].getText());
                ps.setString(7, f[12].getText());
                ps.setInt(8, qty);
                ps.setDouble(9, hb);
                ps.setDouble(10, ppn);
                ps.setDouble(11, hj);
                ps.setDouble(12, hp);
                ps.setString(13, f[11].getText());
                ps.executeUpdate();
                c.prepareStatement(
                        "INSERT INTO riwayat_barang(nama_obat,nomor_batch,tipe,quantity,nama_pbf,harga_beli,total_harga,timestamp) VALUES('"
                                + f[0].getText().replace("'", "''") + "','" + f[4].getText().replace("'", "''")
                                + "','MASUK'," + qty + ",'" + f[6].getText().replace("'", "''") + "'," + hb + ","
                                + (hb * qty) + ",CURRENT_TIMESTAMP)")
                        .executeUpdate();
                showSuccess(p, "Obat berhasil ditambahkan!");
                for (JTextField tf : f)
                    if (tf != null)
                        tf.setText("");
                refreshInv(model);
            } catch (Exception ex) {
                showError(p, ex.getMessage());
            }
        });

        refreshInv(model);
        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(formCard, BorderLayout.CENTER);
        p.add(top, BorderLayout.NORTH);
        p.add(styledScroll(table), BorderLayout.CENTER);
        return p;
    }

    private void refreshInv(DefaultTableModel m) {
        m.setRowCount(0);
        try (Connection c = DriverManager.getConnection(DB, U, P);
                ResultSet rs = c.createStatement().executeQuery("SELECT * FROM obat ORDER BY nama_obat")) {
            while (rs.next())
                m.addRow(new Object[] { rs.getString("no_faktur"), rs.getString("nama_obat"),
                        rs.getString("nomor_batch"), rs.getString("nama_pbf"), rs.getString("satuan"),
                        rs.getString("golongan"), rs.getInt("quantity"), fmt(rs.getDouble("harga_beli")),
                        fmt(rs.getDouble("harga_beli_ppn")), fmt(rs.getDouble("harga_jual_apotek")),
                        fmt(rs.getDouble("harga_plot")) });
        } catch (Exception ignored) {
        }
    }

    private JPanel createPrescriptionPanel() {
        JPanel p = new JPanel(new BorderLayout(16, 16));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        JLabel header = new JLabel("Proses Resep Dokter");
        header.setFont(TITLE);
        header.setForeground(TEXT);

        JPanel formCard = card("Data Resep");
        JPanel formGrid = new JPanel(new GridLayout(7, 2, 12, 8));
        formGrid.setOpaque(false);
        String[] labels = { "Nama Dokter", "Nama Obat", "No Praktek/SIP", "Dosis", "RS/Klinik", "Jumlah", "NIK Pasien",
                "Harga/Item", "Nama Pasien", "Tuslah", "Diagnosa", "Embalase", "Riwayat Alergi", "TOTAL" };
        JTextField[] f = new JTextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            f[i] = styledField(labels[i]);
            if (i == 13) {
                f[i].setEditable(false);
                f[i].setBackground(new Color(220, 252, 231));
                f[i].setFont(new Font("Segoe UI", Font.BOLD, 15));
            }
            formGrid.add(formRow(labels[i] + ":", f[i]));
        }
        KeyAdapter calc = new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                try {
                    double h = parseNum(f[7]), tu = parseNum(f[9]), em = parseNum(f[11]);
                    int q = f[5].getText().isEmpty() ? 1 : Integer.parseInt(f[5].getText());
                    f[13].setText("Rp " + fmt((h * q) + tu + em));
                } catch (Exception ignored) {
                }
            }
        };
        f[5].addKeyListener(calc);
        f[7].addKeyListener(calc);
        f[9].addKeyListener(calc);
        f[11].addKeyListener(calc);
        formCard.add(formGrid, BorderLayout.CENTER);

        JButton btnRx = styledBtn("Proses Resep", SUCCESS);
        JPanel bw = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bw.setOpaque(false);
        bw.add(btnRx);
        formCard.add(bw, BorderLayout.SOUTH);

        JTextArea result = new JTextArea(6, 40);
        result.setEditable(false);
        result.setFont(MONO);
        result.setBackground(new Color(248, 250, 252));
        result.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        btnRx.addActionListener(e -> {
            try (Connection c = DriverManager.getConnection(DB, U, P)) {
                String obat = f[1].getText();
                int qty = f[5].getText().isEmpty() ? 1 : Integer.parseInt(f[5].getText());
                double harga = parseNum(f[7]), tuslah = parseNum(f[9]), embalase = parseNum(f[11]);
                double total = (harga * qty) + tuslah + embalase;
                StringBuilder warn = new StringBuilder();
                String alergi = f[12].getText();
                if ("Antibiotik".equalsIgnoreCase(alergi)
                        && (obat.equalsIgnoreCase("Amoxicillin") || obat.equalsIgnoreCase("Cefadroxil")))
                    warn.append("\u26D4 PASIEN ALERGI ANTIBIOTIK!\n");
                ResultSet rs = c.createStatement()
                        .executeQuery("SELECT quantity FROM obat WHERE nama_obat = '" + obat.replace("'", "''") + "'");
                if (!rs.next() || rs.getInt("quantity") < qty) {
                    result.setText("\u274C Stok tidak cukup!");
                    return;
                }
                if (warn.length() > 0 && JOptionPane.showConfirmDialog(p, warn + "\nTetap lanjutkan?",
                        "\u26A0\uFE0F Peringatan CDSS", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                    return;
                c.createStatement().executeUpdate("UPDATE obat SET quantity = quantity - " + qty
                        + " WHERE nama_obat = '" + obat.replace("'", "''") + "'");
                PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO riwayat_klinis(nik,nama_pasien,diagnosa,nama_obat,dosis,tgl_kunjungan,nama_dokter,no_praktek,nama_rumah_sakit,harga_obat,jumlah_obat,tuslah,embalase,total_harga) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                ps.setString(1, f[6].getText());
                ps.setString(2, f[8].getText());
                ps.setString(3, f[10].getText());
                ps.setString(4, obat);
                ps.setString(5, f[3].getText());
                ps.setDate(6, Date.valueOf(LocalDate.now()));
                ps.setString(7, f[0].getText());
                ps.setString(8, f[2].getText());
                ps.setString(9, f[4].getText());
                ps.setDouble(10, harga);
                ps.setInt(11, qty);
                ps.setDouble(12, tuslah);
                ps.setDouble(13, embalase);
                ps.setDouble(14, total);
                ps.executeUpdate();
                result.setText("\u2705 RESEP BERHASIL DIPROSES\n\nDokter: " + f[0].getText() + " | RS: "
                        + f[4].getText() + "\nPasien: " + f[8].getText() + "\nObat: " + obat + " x" + qty
                        + "\nHarga: Rp " + fmt(harga) + " | Tuslah: Rp " + fmt(tuslah) + " | Embalase: Rp "
                        + fmt(embalase) + "\n\n\uD83D\uDCB0 TOTAL: Rp " + fmt(total));
            } catch (Exception ex) {
                result.setText("\u274C " + ex.getMessage());
            }
        });

        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(formCard, BorderLayout.CENTER);
        p.add(top, BorderLayout.NORTH);
        JScrollPane rsp = new JScrollPane(result);
        rsp.setBorder(new javax.swing.border.LineBorder(BORDER, 1, true));
        p.add(rsp, BorderLayout.CENTER);
        return p;
    }

    private JPanel createOpnamePanel() {
        JPanel p = new JPanel(new BorderLayout(16, 16));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        JLabel header = new JLabel("Stok Opname & Audit");
        header.setFont(TITLE);
        header.setForeground(TEXT);

        JPanel formCard = card(null);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        row.setOpaque(false);
        JTextField tfF = styledField("No Faktur"), tfS = styledField("Stok Fisik");
        tfF.setPreferredSize(new Dimension(200, 40));
        tfS.setPreferredSize(new Dimension(120, 40));
        JButton btnC = styledBtn("\uD83D\uDD0D Cek Stok", PRIMARY), btnA = styledBtn("\uD83D\uDD04 Sesuaikan", WARNING);
        row.add(tfF);
        row.add(tfS);
        row.add(btnC);
        row.add(btnA);
        formCard.add(row, BorderLayout.CENTER);

        JTextArea res = new JTextArea(2, 40);
        res.setEditable(false);
        res.setFont(new Font("Segoe UI", Font.BOLD, 15));
        res.setBackground(new Color(248, 250, 252));
        res.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        formCard.add(res, BorderLayout.SOUTH);

        DefaultTableModel model = new DefaultTableModel(
                new String[] { "Waktu", "No Faktur", "Obat", "Stok Sistem", "Stok Fisik", "Selisih", "Status" }, 0);
        JTable table = new JTable(model);

        btnC.addActionListener(e -> {
            try (Connection c = DriverManager.getConnection(DB, U, P)) {
                ResultSet rs = c.createStatement().executeQuery(
                        "SELECT * FROM obat WHERE no_faktur = '" + tfF.getText().replace("'", "''") + "'");
                if (!rs.next()) {
                    res.setText("Obat tidak ditemukan");
                    return;
                }
                int sistem = rs.getInt("quantity"), fisik = Integer.parseInt(tfS.getText()), sel = fisik - sistem;
                double persen = sistem > 0 ? (Math.abs(sel) * 100.0 / sistem) : (sel == 0 ? 0 : 100);
                String status = (persen > 5.0 && Math.abs(sel) > 1) ? "SELISIH TINGGI" : "NORMAL";
                PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO stok_opname(nama_obat,no_faktur,stok_sistem,stok_fisik,selisih,status,tanggal) VALUES(?,?,?,?,?,?,?)");
                ps.setString(1, rs.getString("nama_obat"));
                ps.setString(2, tfF.getText());
                ps.setInt(3, sistem);
                ps.setInt(4, fisik);
                ps.setInt(5, sel);
                ps.setString(6, status);
                ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
                String icon = status.equals("NORMAL") ? "TOLERANSI OK" : "PERLU APPROVAL";
                res.setText(icon + " | " + status + " (" + String.format("%.1f", persen) + "%)  |  Sistem: " + sistem
                        + "  |  Fisik: " + fisik + "  |  Selisih: " + sel);
                if (status.equals("NORMAL")) {
                    c.createStatement().executeUpdate("UPDATE obat SET quantity = " + fisik + " WHERE no_faktur = '"
                            + tfF.getText().replace("'", "''") + "'");
                    res.setText(res.getText() + "\n  >> Auto-adjust: stok diperbarui ke " + fisik);
                }
                refreshOpn(model);
            } catch (Exception ex) {
                res.setText("Error: " + ex.getMessage());
            }
        });

        btnA.addActionListener(e -> {
            try (Connection c = DriverManager.getConnection(DB, U, P)) {
                int fisik = Integer.parseInt(tfS.getText());
                ResultSet rs = c.createStatement().executeQuery(
                        "SELECT quantity FROM obat WHERE no_faktur = '" + tfF.getText().replace("'", "''") + "'");
                if (!rs.next()) {
                    res.setText("Obat tidak ditemukan");
                    return;
                }
                int sistem = rs.getInt("quantity");
                double persen = sistem > 0 ? (Math.abs(fisik - sistem) * 100.0 / sistem) : 100;
                if (persen > 5.0 && Math.abs(fisik - sistem) > 1) {
                    int confirm = JOptionPane.showConfirmDialog(p,
                            "SELISIH TINGGI (" + String.format("%.1f", persen) + "%)\n"
                                    + "Sistem: " + sistem + " | Fisik: " + fisik + " | Selisih: " + (fisik - sistem)
                                    + "\n\nApakah Anda yakin ingin menyesuaikan stok?",
                            "Approval Penyesuaian Stok", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (confirm != JOptionPane.YES_OPTION) {
                        res.setText("Penyesuaian dibatalkan");
                        return;
                    }
                }
                c.createStatement().executeUpdate("UPDATE obat SET quantity = " + fisik + " WHERE no_faktur = '"
                        + tfF.getText().replace("'", "''") + "'");
                res.setText("Stok disesuaikan ke " + fisik + " unit (selisih " + String.format("%.1f", persen) + "%)");
                refreshOpn(model);
            } catch (Exception ex) {
                res.setText("Error: " + ex.getMessage());
            }
        });

        refreshOpn(model);
        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(formCard, BorderLayout.CENTER);
        p.add(top, BorderLayout.NORTH);
        p.add(styledScroll(table), BorderLayout.CENTER);
        return p;
    }

    private void refreshOpn(DefaultTableModel m) {
        m.setRowCount(0);
        try (Connection c = DriverManager.getConnection(DB, U, P);
                ResultSet rs = c.createStatement().executeQuery("SELECT * FROM stok_opname ORDER BY tanggal DESC")) {
            while (rs.next())
                m.addRow(new Object[] { rs.getTimestamp("tanggal"), rs.getString("no_faktur"),
                        rs.getString("nama_obat"), rs.getInt("stok_sistem"), rs.getInt("stok_fisik"),
                        rs.getInt("selisih"), rs.getString("status") });
        } catch (Exception ignored) {
        }
    }

    private JPanel createReportsPanel() {
        JPanel p = new JPanel(new BorderLayout(16, 16));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        JLabel header = new JLabel("Laporan & Export");
        header.setFont(TITLE);
        header.setForeground(TEXT);

        JPanel controls = card(null);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);
        JTextField tfDate = styledField("YYYY-MM-DD");
        tfDate.setText(LocalDate.now().toString());
        tfDate.setPreferredSize(new Dimension(140, 40));
        JButton b1 = styledBtn("\uD83D\uDCC8 Omset Harian", PRIMARY),
                b2 = styledBtn("\uD83D\uDCE5 Excel Omset", SUCCESS),
                b3 = styledBtn("\uD83D\uDCE5 Excel PBF", new Color(99, 102, 241)),
                b4 = styledBtn("\uD83D\uDCC4 PDF Report", DANGER);
        row.add(tfDate);
        row.add(b1);
        row.add(b2);
        row.add(b3);
        row.add(b4);
        controls.add(row, BorderLayout.CENTER);
        JTextArea res = new JTextArea(2, 40);
        res.setEditable(false);
        res.setFont(new Font("Segoe UI", Font.BOLD, 16));
        res.setBackground(new Color(248, 250, 252));
        res.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        controls.add(res, BorderLayout.SOUTH);

        DefaultTableModel model = new DefaultTableModel(
                new String[] { "Waktu", "Obat", "Tipe", "Qty", "PBF", "H.Beli", "Total" }, 0);
        JTable table = new JTable(model);

        b1.addActionListener(e -> {
            try (Connection c = DriverManager.getConnection(DB, U, P)) {
                LocalDate d = LocalDate.parse(tfDate.getText());
                double t = 0;
                ResultSet rs = c.createStatement().executeQuery(
                        "SELECT COALESCE(SUM(total_harga),0) as t FROM riwayat_klinis WHERE tgl_kunjungan = '" + d
                                + "'");
                if (rs.next())
                    t += rs.getDouble("t");
                ResultSet rs2 = c.createStatement().executeQuery(
                        "SELECT COALESCE(SUM(total_harga),0) as t FROM penjualan WHERE CAST(tanggal AS DATE) = '" + d
                                + "'");
                if (rs2.next())
                    t += rs2.getDouble("t");
                res.setText("\uD83D\uDCB0 Omset " + d + ": Rp " + fmt(t));
            } catch (Exception ex) {
                res.setText("\u274C " + ex.getMessage());
            }
        });
        b2.addActionListener(e -> exportOmsetExcel());
        b3.addActionListener(e -> exportPBFExcel());
        b4.addActionListener(e -> exportPDF());

        refreshRep(model);
        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(controls, BorderLayout.CENTER);
        p.add(top, BorderLayout.NORTH);
        p.add(styledScroll(table), BorderLayout.CENTER);
        return p;
    }

    private void refreshRep(DefaultTableModel m) {
        m.setRowCount(0);
        try (Connection c = DriverManager.getConnection(DB, U, P);
                ResultSet rs = c.createStatement()
                        .executeQuery("SELECT * FROM riwayat_barang ORDER BY timestamp DESC")) {
            while (rs.next())
                m.addRow(new Object[] { rs.getTimestamp("timestamp"), rs.getString("nama_obat"), rs.getString("tipe"),
                        rs.getInt("quantity"), rs.getString("nama_pbf"), fmt(rs.getDouble("harga_beli")),
                        fmt(rs.getDouble("total_harga")) });
        } catch (Exception ignored) {
        }
    }

    private JPanel createPenjualanPanel() {
        JPanel p = new JPanel(new BorderLayout(16, 16));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        JLabel header = new JLabel("Penjualan Langsung (OTC)");
        header.setFont(TITLE);
        header.setForeground(TEXT);

        JPanel formCard = card(null);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);
        JTextField tfB = styledField("Nama Pembeli"), tfO = styledField("Nama Obat"), tfQ = styledField("Qty");
        tfB.setPreferredSize(new Dimension(200, 40));
        tfO.setPreferredSize(new Dimension(200, 40));
        tfQ.setPreferredSize(new Dimension(80, 40));
        JButton btnA = styledBtn("\u2795 Tambah", PRIMARY), btnP = styledBtn("\u2705 Proses", SUCCESS);
        JLabel lblT = new JLabel("Total: Rp 0");
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblT.setForeground(PRIMARY);
        row.add(tfB);
        row.add(tfO);
        row.add(tfQ);
        row.add(btnA);
        row.add(lblT);
        row.add(btnP);
        formCard.add(row, BorderLayout.CENTER);

        DefaultTableModel cart = new DefaultTableModel(new String[] { "Obat", "Qty", "Harga", "Subtotal" }, 0);
        JTable cartT = new JTable(cart);
        DefaultTableModel hist = new DefaultTableModel(new String[] { "No Transaksi", "Tanggal", "Pembeli", "Total" },
                0);
        JTable histT = new JTable(hist);

        btnA.addActionListener(e -> {
            try (Connection c = DriverManager.getConnection(DB, U, P)) {
                String nama = tfO.getText();
                int qty = Integer.parseInt(tfQ.getText());
                ResultSet rs = c.createStatement()
                        .executeQuery("SELECT harga_jual_apotek, harga_beli FROM obat WHERE nama_obat = '"
                                + nama.replace("'", "''") + "'");
                if (rs.next()) {
                    double h = rs.getDouble("harga_jual_apotek");
                    if (h == 0)
                        h = rs.getDouble("harga_beli");
                    cart.addRow(new Object[] { nama, qty, fmt(h), fmt(h * qty) });
                    double t = 0;
                    for (int i = 0; i < cart.getRowCount(); i++)
                        t += parseFmt((String) cart.getValueAt(i, 3));
                    lblT.setText("Total: Rp " + fmt(t));
                } else
                    showError(p, "Obat tidak ditemukan");
                tfO.setText("");
                tfQ.setText("");
            } catch (Exception ex) {
                showError(p, ex.getMessage());
            }
        });

        btnP.addActionListener(e -> {
            try (Connection c = DriverManager.getConnection(DB, U, P)) {
                String noTrx = "TRX-" + System.currentTimeMillis();
                double gt = 0;
                for (int i = 0; i < cart.getRowCount(); i++) {
                    String nama = (String) cart.getValueAt(i, 0);
                    int qty = (int) cart.getValueAt(i, 1);
                    double sub = parseFmt((String) cart.getValueAt(i, 3));
                    gt += sub;
                    c.createStatement().executeUpdate("UPDATE obat SET quantity = quantity - " + qty
                            + " WHERE nama_obat = '" + nama.replace("'", "''") + "'");
                }
                PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO penjualan(no_transaksi,tanggal,nama_pembeli,total_harga) VALUES(?,?,?,?)");
                ps.setString(1, noTrx);
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.setString(3, tfB.getText());
                ps.setDouble(4, gt);
                ps.executeUpdate();
                showSuccess(p, "Penjualan berhasil! " + noTrx + " | Rp " + fmt(gt));
                cart.setRowCount(0);
                lblT.setText("Total: Rp 0");
                tfB.setText("");
                refreshHist(hist);
            } catch (Exception ex) {
                showError(p, ex.getMessage());
            }
        });

        refreshHist(hist);
        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(formCard, BorderLayout.CENTER);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, styledScroll(cartT), styledScroll(histT));
        split.setDividerLocation(180);
        split.setOpaque(false);
        p.add(top, BorderLayout.NORTH);
        p.add(split, BorderLayout.CENTER);
        return p;
    }

    private void refreshHist(DefaultTableModel m) {
        m.setRowCount(0);
        try (Connection c = DriverManager.getConnection(DB, U, P);
                ResultSet rs = c.createStatement().executeQuery("SELECT * FROM penjualan ORDER BY tanggal DESC")) {
            while (rs.next())
                m.addRow(new Object[] { rs.getString("no_transaksi"), rs.getTimestamp("tanggal"),
                        rs.getString("nama_pembeli"), "Rp " + fmt(rs.getDouble("total_harga")) });
        } catch (Exception ignored) {
        }
    }

    private JPanel createMIMSPanel() {
        JPanel p = new JPanel(new BorderLayout(16, 16));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        JLabel header = new JLabel("MIMS Apotek - Referensi Obat");
        header.setFont(TITLE);
        header.setForeground(TEXT);

        JPanel searchCard = card(null);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        row.setOpaque(false);
        JTextField tfS = styledField("Cari obat...");
        tfS.setPreferredSize(new Dimension(350, 40));
        JButton btnS = styledBtn("\uD83D\uDD0D Cari", PRIMARY);
        row.add(tfS);
        row.add(btnS);
        searchCard.add(row, BorderLayout.CENTER);

        DefaultTableModel model = new DefaultTableModel(new String[] { "Nama Obat", "Golongan", "Satuan",
                "Indikasi/Kegunaan", "H.Jual Apotek", "H.Plot", "Stok" }, 0);
        JTable table = new JTable(model);

        Runnable search = () -> {
            model.setRowCount(0);
            String kw = tfS.getText().trim();
            String sql = kw.isEmpty() ? "SELECT * FROM obat ORDER BY nama_obat"
                    : "SELECT * FROM obat WHERE LOWER(nama_obat) LIKE '%" + kw.toLowerCase().replace("'", "''")
                            + "%' ORDER BY nama_obat";
            try (Connection c = DriverManager.getConnection(DB, U, P);
                    ResultSet rs = c.createStatement().executeQuery(sql)) {
                while (rs.next())
                    model.addRow(new Object[] { rs.getString("nama_obat"), rs.getString("golongan"),
                            rs.getString("satuan"), rs.getString("indikasi"), fmt(rs.getDouble("harga_jual_apotek")),
                            fmt(rs.getDouble("harga_plot")), rs.getInt("quantity") });
            } catch (Exception ignored) {
            }
        };
        btnS.addActionListener(e -> search.run());
        tfS.addActionListener(e -> search.run());
        search.run();

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(searchCard, BorderLayout.CENTER);
        p.add(top, BorderLayout.NORTH);
        p.add(styledScroll(table), BorderLayout.CENTER);
        return p;
    }

    private void exportOmsetExcel() {
        try {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("Laporan_Omset.xlsx"));
            if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
                return;
            try (XSSFWorkbook wb = new XSSFWorkbook(); Connection c = DriverManager.getConnection(DB, U, P)) {
                XSSFCellStyle hs = wb.createCellStyle();
                XSSFFont hf = wb.createFont();
                hf.setBold(true);
                hs.setFont(hf);
                XSSFSheet s = wb.createSheet("Omset Resep");
                XSSFRow h = s.createRow(0);
                String[] cols = { "No", "Tanggal", "Pasien", "Dokter", "Obat", "Qty", "Harga", "Tuslah", "Embalase",
                        "Total" };
                for (int i = 0; i < cols.length; i++) {
                    XSSFCell cell = h.createCell(i);
                    cell.setCellValue(cols[i]);
                    cell.setCellStyle(hs);
                }
                ResultSet rs = c.createStatement()
                        .executeQuery("SELECT * FROM riwayat_klinis ORDER BY tgl_kunjungan DESC");
                int row = 1;
                double total = 0;
                while (rs.next()) {
                    XSSFRow r = s.createRow(row);
                    r.createCell(0).setCellValue(row);
                    r.createCell(1).setCellValue(String.valueOf(rs.getDate("tgl_kunjungan")));
                    r.createCell(2).setCellValue(rs.getString("nama_pasien"));
                    r.createCell(3).setCellValue(rs.getString("nama_dokter"));
                    r.createCell(4).setCellValue(rs.getString("nama_obat"));
                    r.createCell(5).setCellValue(rs.getInt("jumlah_obat"));
                    r.createCell(6).setCellValue(rs.getDouble("harga_obat"));
                    r.createCell(7).setCellValue(rs.getDouble("tuslah"));
                    r.createCell(8).setCellValue(rs.getDouble("embalase"));
                    r.createCell(9).setCellValue(rs.getDouble("total_harga"));
                    total += rs.getDouble("total_harga");
                    row++;
                }
                XSSFRow tr = s.createRow(row);
                tr.createCell(8).setCellValue("TOTAL:");
                tr.createCell(9).setCellValue(total);
                for (int i = 0; i < cols.length; i++)
                    s.autoSizeColumn(i);
                try (FileOutputStream fos = new FileOutputStream(fc.getSelectedFile())) {
                    wb.write(fos);
                }
                showSuccess(this, "Excel berhasil disimpan!");
            }
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    private void exportPBFExcel() {
        try {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("Laporan_PBF.xlsx"));
            if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
                return;
            try (XSSFWorkbook wb = new XSSFWorkbook(); Connection c = DriverManager.getConnection(DB, U, P)) {
                XSSFCellStyle hs = wb.createCellStyle();
                XSSFFont hf = wb.createFont();
                hf.setBold(true);
                hs.setFont(hf);
                XSSFSheet s = wb.createSheet("PBF");
                XSSFRow h = s.createRow(0);
                String[] cols = { "No", "Waktu", "Obat", "Batch", "PBF", "Qty", "H.Beli", "Total" };
                for (int i = 0; i < cols.length; i++) {
                    XSSFCell cell = h.createCell(i);
                    cell.setCellValue(cols[i]);
                    cell.setCellStyle(hs);
                }
                ResultSet rs = c.createStatement()
                        .executeQuery("SELECT * FROM riwayat_barang WHERE tipe='MASUK' ORDER BY timestamp DESC");
                int row = 1;
                double total = 0;
                while (rs.next()) {
                    XSSFRow r = s.createRow(row);
                    r.createCell(0).setCellValue(row);
                    r.createCell(1).setCellValue(String.valueOf(rs.getTimestamp("timestamp")));
                    r.createCell(2).setCellValue(rs.getString("nama_obat"));
                    r.createCell(3).setCellValue(rs.getString("nomor_batch"));
                    r.createCell(4).setCellValue(rs.getString("nama_pbf"));
                    r.createCell(5).setCellValue(rs.getInt("quantity"));
                    r.createCell(6).setCellValue(rs.getDouble("harga_beli"));
                    r.createCell(7).setCellValue(rs.getDouble("total_harga"));
                    total += rs.getDouble("total_harga");
                    row++;
                }
                XSSFRow tr = s.createRow(row);
                tr.createCell(6).setCellValue("TOTAL:");
                tr.createCell(7).setCellValue(total);
                for (int i = 0; i < cols.length; i++)
                    s.autoSizeColumn(i);
                try (FileOutputStream fos = new FileOutputStream(fc.getSelectedFile())) {
                    wb.write(fos);
                }
                showSuccess(this, "Excel PBF berhasil disimpan!");
            }
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    private void exportPDF() {
        try {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("Laporan_Apotek.pdf"));
            if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
                return;
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(fc.getSelectedFile()));
            doc.open();
            doc.add(new Paragraph("LAPORAN APOTEK - " + LocalDate.now()));
            doc.add(new Paragraph(" "));
            try (Connection c = DriverManager.getConnection(DB, U, P)) {
                ResultSet rs = c.createStatement().executeQuery("SELECT * FROM riwayat_barang ORDER BY timestamp DESC");
                while (rs.next())
                    doc.add(new Paragraph(rs.getTimestamp("timestamp") + " | " + rs.getString("tipe") + " | "
                            + rs.getString("nama_obat") + " | Qty: " + rs.getInt("quantity") + " | PBF: "
                            + rs.getString("nama_pbf")));
            }
            doc.close();
            showSuccess(this, "PDF berhasil disimpan!");
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    private void ensureTableExists() {
        try (Connection c = DriverManager.getConnection(DB, U, P); Statement s = c.createStatement()) {
            s.execute(
                    "CREATE TABLE IF NOT EXISTS obat (id BIGINT AUTO_INCREMENT PRIMARY KEY, nama_obat VARCHAR(255), nomor_batch VARCHAR(255), tgl_produksi DATE, tgl_expired DATE, supplier VARCHAR(255), no_faktur VARCHAR(255) UNIQUE, satuan VARCHAR(100), nama_pbf VARCHAR(255), harga_beli DOUBLE, harga_jual_apotek DOUBLE, harga_plot DOUBLE, harga_beli_ppn DOUBLE, quantity INT, golongan VARCHAR(100), indikasi VARCHAR(1000))");
            s.execute(
                    "CREATE TABLE IF NOT EXISTS riwayat_barang (id BIGINT AUTO_INCREMENT PRIMARY KEY, nama_obat VARCHAR(255), nomor_batch VARCHAR(255), tipe VARCHAR(20), quantity INT, timestamp TIMESTAMP, nama_pbf VARCHAR(255), harga_beli DOUBLE, total_harga DOUBLE)");
            s.execute(
                    "CREATE TABLE IF NOT EXISTS riwayat_klinis (id BIGINT AUTO_INCREMENT PRIMARY KEY, nik VARCHAR(50), nama_pasien VARCHAR(255), diagnosa VARCHAR(500), nama_obat VARCHAR(255), dosis VARCHAR(100), tgl_kunjungan DATE, nama_dokter VARCHAR(255), no_praktek VARCHAR(100), nama_rumah_sakit VARCHAR(255), harga_obat DOUBLE, jumlah_obat INT, tuslah DOUBLE, embalase DOUBLE, total_harga DOUBLE)");
            s.execute(
                    "CREATE TABLE IF NOT EXISTS stok_opname (id BIGINT AUTO_INCREMENT PRIMARY KEY, nama_obat VARCHAR(255), no_faktur VARCHAR(255), stok_sistem INT, stok_fisik INT, selisih INT, status VARCHAR(50), tanggal TIMESTAMP)");
            s.execute(
                    "CREATE TABLE IF NOT EXISTS penjualan (id BIGINT AUTO_INCREMENT PRIMARY KEY, no_transaksi VARCHAR(100) UNIQUE, tanggal TIMESTAMP, nama_pembeli VARCHAR(255), total_harga DOUBLE)");
            s.execute(
                    "CREATE TABLE IF NOT EXISTS detail_penjualan (id BIGINT AUTO_INCREMENT PRIMARY KEY, penjualan_id BIGINT, nama_obat VARCHAR(255), harga_jual DOUBLE, jumlah INT, subtotal DOUBLE)");
            String[][] alts = { { "obat", "no_faktur VARCHAR(255)" }, { "obat", "satuan VARCHAR(100)" },
                    { "obat", "nama_pbf VARCHAR(255)" }, { "obat", "harga_jual_apotek DOUBLE" },
                    { "obat", "harga_plot DOUBLE" }, { "obat", "harga_beli_ppn DOUBLE" },
                    { "obat", "golongan VARCHAR(100)" }, { "obat", "indikasi VARCHAR(1000)" },
                    { "riwayat_barang", "nama_pbf VARCHAR(255)" }, { "riwayat_barang", "harga_beli DOUBLE" },
                    { "riwayat_barang", "total_harga DOUBLE" }, { "riwayat_klinis", "nama_dokter VARCHAR(255)" },
                    { "riwayat_klinis", "no_praktek VARCHAR(100)" },
                    { "riwayat_klinis", "nama_rumah_sakit VARCHAR(255)" }, { "riwayat_klinis", "harga_obat DOUBLE" },
                    { "riwayat_klinis", "jumlah_obat INT" }, { "riwayat_klinis", "tuslah DOUBLE" },
                    { "riwayat_klinis", "embalase DOUBLE" }, { "riwayat_klinis", "total_harga DOUBLE" } };
            for (String[] a : alts)
                try {
                    s.execute("ALTER TABLE " + a[0] + " ADD COLUMN IF NOT EXISTS " + a[1]);
                } catch (Exception ignored) {
                }
        } catch (Exception e) {
            System.err.println("DB: " + e.getMessage());
        }
    }

    private String fmt(double v) {
        return String.format("%,.0f", v);
    }

    private double parseNum(JTextField f) {
        try {
            return Double.parseDouble(f.getText());
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseFmt(String s) {
        try {
            return Double.parseDouble(s.replace("Rp ", "").replace(",", "").replace(".", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private void showSuccess(Component p, String msg) {
        JOptionPane.showMessageDialog(p, msg, "\u2705 Berhasil", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(Component p, String msg) {
        JOptionPane.showMessageDialog(p, msg, "\u274C Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            FlatLightLaf.setup();
        } catch (Throwable ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e2) {
            }
        }
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.trackArc", 999);
        UIManager.put("ScrollBar.thumbArc", 999);
        SwingUtilities.invokeLater(() -> new MyApotekApp().setVisible(true));
    }
}
