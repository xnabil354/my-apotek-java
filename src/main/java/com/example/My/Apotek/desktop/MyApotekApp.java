package com.example.My.Apotek.desktop;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Phrase;
import com.lowagie.text.PageSize;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class MyApotekApp extends JFrame {
    // Komponen Alur 1
    private JTextField txtScanBarcode;
    private JTextField txtNamaObat, txtBatch, txtTglProd, txtExp, txtSupplier, txtHarga, txtQty;

    // Komponen Alur 2
    private JTextField txtNik, txtNamaPasien, txtUsia, txtBB, txtDosis;
    private JTextField txtDiagnosa, txtGFR, txtMedikasiLain;
    private JComboBox<String> cbAlergi, cbObatResep;

    // Komponen Alur 3
    private JTextField txtScanStok;
    private JTextField txtStokSistem, txtStokFisik;

    private JTextArea txtResult;
    private JButton btnDispense;
    private JButton btnCekStok; // Added for Global Access

    public MyApotekApp() {
        // 1. SET MODERN THEME (Nimbus with Flat Customization)
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    UIManager.getLookAndFeelDefaults().put("TextField.contentMargins", new Insets(5, 10, 5, 10));
                    UIManager.getLookAndFeelDefaults().put("Button.contentMargins", new Insets(8, 15, 8, 15));
                    break;
                }
            }
            Font modernFont = new Font("Segoe UI", Font.PLAIN, 13);
            UIManager.put("Label.font", modernFont);
            UIManager.put("TextField.font", modernFont);
            UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 13));
            UIManager.put("TabbedPane.font", new Font("Segoe UI", Font.BOLD, 14));
        } catch (Exception e) {
        }

        setTitle("My Apotek Professional System v2.0");
        setSize(1000, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null); // Center

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(245, 247, 250));

        // --- TAB 1: BARANG DATANG ---
        JPanel p1 = new JPanel(new GridBagLayout());
        p1.setBackground(Color.WHITE);
        p1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel title1 = new JLabel("INBOUND INVENTORY (BARANG MASUK)");
        title1.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title1.setForeground(new Color(44, 62, 80));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        p1.add(title1, gbc);

        // Scan Section
        JPanel pScan = new JPanel(new BorderLayout(10, 0));
        pScan.setBackground(new Color(236, 240, 241));
        pScan.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel lblScan = new JLabel("⚡ QUICK SCAN BARCODE / SKU (Press Enter):");
        lblScan.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblScan.setForeground(new Color(39, 174, 96));
        txtScanBarcode = new JTextField();
        txtScanBarcode.setFont(new Font("Consolas", Font.BOLD, 16));
        txtScanBarcode.addActionListener(e -> handleScanBarcodeAlur1());

        pScan.add(lblScan, BorderLayout.NORTH);
        pScan.add(txtScanBarcode, BorderLayout.CENTER);
        gbc.gridy = 1;
        p1.add(pScan, gbc);

        // Form Grid
        JPanel pForm1 = new JPanel(new GridLayout(4, 2, 15, 15));
        pForm1.setBackground(Color.WHITE);
        pForm1.setBorder(BorderFactory.createTitledBorder("Product Details"));

        txtNamaObat = createStyledField();
        txtBatch = createStyledField();
        txtTglProd = createStyledField("2024-01-01");
        txtExp = createStyledField("2026-06-01");
        txtSupplier = createStyledField();
        txtHarga = createStyledField();
        txtQty = createStyledField();

        addLabelAndField(pForm1, "Nama Obat:", txtNamaObat);
        addLabelAndField(pForm1, "Nomor Batch:", txtBatch);
        addLabelAndField(pForm1, "Tgl Produksi:", txtTglProd);
        addLabelAndField(pForm1, "Tgl Expired:", txtExp);
        addLabelAndField(pForm1, "Supplier:", txtSupplier);
        addLabelAndField(pForm1, "Harga Beli (Rp):", txtHarga);
        addLabelAndField(pForm1, "Quantity (+):", txtQty);

        gbc.gridy = 2;
        p1.add(pForm1, gbc);

        JButton btnSimpanBarang = new JButton("💾 SIMPAN DATA (SAVE & PRINT BARCODE)");
        btnSimpanBarang.setBackground(new Color(41, 128, 185));
        btnSimpanBarang.setForeground(Color.WHITE);
        btnSimpanBarang.setPreferredSize(new Dimension(200, 45));
        btnSimpanBarang.addActionListener(e -> handleSimpanBarang());

        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        p1.add(btnSimpanBarang, gbc);

        tabbedPane.addTab("📦 Barang Datang", new JScrollPane(p1));

        // --- TAB 2: INPUT RESEP ---
        JPanel p2 = new JPanel(new BorderLayout(15, 15));
        p2.setBackground(new Color(245, 247, 250));
        p2.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel p2Header = new JPanel(new BorderLayout());
        p2Header.setBackground(new Color(245, 247, 250));
        JLabel title2 = new JLabel("PRESCRIPTION PROCESSING & CDSS VALIDATION");
        title2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title2.setForeground(new Color(44, 62, 80));
        p2Header.add(title2, BorderLayout.WEST);
        p2.add(p2Header, BorderLayout.NORTH);

        JPanel p2Content = new JPanel(new GridLayout(1, 2, 20, 0));
        p2Content.setBackground(new Color(245, 247, 250));

        // LEFT: PATIENT DATA
        JPanel pPasien = new JPanel(new GridBagLayout());
        pPasien.setBorder(createModernBorder("Data Pasien (Patient Info)"));
        pPasien.setBackground(Color.WHITE);

        txtNik = createStyledField();
        txtNamaPasien = createStyledField();
        txtUsia = createStyledField();
        txtBB = createStyledField();
        txtGFR = createStyledField();
        cbAlergi = new JComboBox<>(new String[] { "Tidak Ada", "Paracetamol", "Amoxicillin", "Antibiotik" });
        txtMedikasiLain = createStyledField();

        GridBagConstraints gbcL = new GridBagConstraints();
        gbcL.fill = GridBagConstraints.HORIZONTAL;
        gbcL.insets = new Insets(5, 5, 5, 5);
        gbcL.weightx = 1.0;

        addFormRow(pPasien, gbcL, 0, "NIK:", txtNik);
        addFormRow(pPasien, gbcL, 1, "Nama Pasien:", txtNamaPasien);
        addFormRow(pPasien, gbcL, 2, "Usia (Tahun):", txtUsia);
        addFormRow(pPasien, gbcL, 3, "Berat Badan (kg):", txtBB);
        addFormRow(pPasien, gbcL, 4, "Fungsi Ginjal (GFR):", txtGFR);
        addFormRow(pPasien, gbcL, 5, "Riwayat Alergi:", cbAlergi);
        addFormRow(pPasien, gbcL, 6, "Medikasi Lain:", txtMedikasiLain);

        // RIGHT: CLINICAL
        JPanel pKlinis = new JPanel(new GridBagLayout());
        pKlinis.setBorder(createModernBorder("Data Klinis & Resep (Clinical)"));
        pKlinis.setBackground(Color.WHITE);

        txtDiagnosa = createStyledField();
        JPanel pMode = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pMode.setBackground(Color.WHITE);
        JRadioButton rbManual = new JRadioButton("Manual Input", true);
        JRadioButton rbOCR = new JRadioButton("Scan AI OCR");
        rbManual.setBackground(Color.WHITE);
        rbOCR.setBackground(Color.WHITE);
        ButtonGroup bgMode = new ButtonGroup();
        bgMode.add(rbManual);
        bgMode.add(rbOCR);
        pMode.add(rbManual);
        pMode.add(rbOCR);

        JButton btnScanResep = new JButton("📷 Scan/Upload Resep");
        btnScanResep.setEnabled(false);
        btnScanResep.addActionListener(e -> handleScanResepOCR());

        cbObatResep = new JComboBox<>(
                new String[] { "Paracetamol", "Amoxicillin", "Cefadroxil", "Ketorolac", "Asam Mefenamat" });
        txtDosis = createStyledField();

        rbManual.addActionListener(e -> {
            btnScanResep.setEnabled(false);
            cbObatResep.setEnabled(true);
        });
        rbOCR.addActionListener(e -> {
            btnScanResep.setEnabled(true);
            cbObatResep.setEnabled(false);
        });

        GridBagConstraints gbcR = new GridBagConstraints();
        gbcR.fill = GridBagConstraints.HORIZONTAL;
        gbcR.insets = new Insets(5, 5, 5, 5);
        gbcR.weightx = 1.0;

        addFormRow(pKlinis, gbcR, 0, "Diagnosa Dokter:", txtDiagnosa);
        addFormRow(pKlinis, gbcR, 1, "Mode Input:", pMode);
        addFormRow(pKlinis, gbcR, 2, "", btnScanResep);
        addFormRow(pKlinis, gbcR, 3, "Pilih Obat:", cbObatResep);
        addFormRow(pKlinis, gbcR, 4, "Dosis (mg):", txtDosis);

        JButton btnAnalisis = new JButton("🔍 JALANKAN VALIDASI (CHECK)");
        btnAnalisis.setBackground(new Color(230, 126, 34));
        btnAnalisis.setForeground(Color.WHITE);
        btnAnalisis.addActionListener(e -> handleAnalisisResep());

        btnDispense = new JButton("✅ APPROVE & DISPENSE");
        btnDispense.setEnabled(false);
        btnDispense.setBackground(new Color(39, 174, 96));
        btnDispense.setForeground(Color.WHITE);
        btnDispense.addActionListener(e -> handleDispense());

        JPanel pActions = new JPanel(new GridLayout(1, 2, 10, 0));
        pActions.add(btnAnalisis);
        pActions.add(btnDispense);

        gbcR.gridy = 10;
        gbcR.gridwidth = 2;
        gbcR.insets = new Insets(20, 5, 5, 5);
        pKlinis.add(pActions, gbcR);

        p2Content.add(pPasien);
        p2Content.add(pKlinis);
        p2.add(p2Content, BorderLayout.CENTER);

        tabbedPane.addTab("💊 Input Resep", new JScrollPane(p2));

        // --- TAB 3: ALUR STOK OPNAME ---
        JPanel p3 = new JPanel(new GridBagLayout());
        p3.setBackground(Color.WHITE);
        p3.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Form Panel
        JPanel pOpname = new JPanel(new GridBagLayout());
        pOpname.setBackground(Color.WHITE);
        pOpname.setBorder(createModernBorder("Stock Opname (Audit Flow)"));

        GridBagConstraints gbc3 = new GridBagConstraints();
        gbc3.insets = new Insets(10, 10, 10, 10);
        gbc3.fill = GridBagConstraints.HORIZONTAL;

        // 1. Scan Section
        gbc3.gridx = 0;
        gbc3.gridy = 0;
        gbc3.weightx = 0.3;
        JLabel lblScan3 = new JLabel("1. SCAN BARCODE / SKU:");
        lblScan3.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pOpname.add(lblScan3, gbc3);

        gbc3.gridx = 1;
        gbc3.weightx = 0.7;
        txtScanStok = createStyledField();
        txtScanStok.setBackground(new Color(255, 243, 205)); // Yellow tint for focus
        txtScanStok.addActionListener(e -> handleScanStokOpname());
        pOpname.add(txtScanStok, gbc3);

        // 2. System Stock (Read Only)
        gbc3.gridx = 0;
        gbc3.gridy = 1;
        gbc3.weightx = 0.3;
        pOpname.add(new JLabel("2. Stok Sistem (Database):"), gbc3);

        gbc3.gridx = 1;
        gbc3.weightx = 0.7;
        txtStokSistem = createStyledField();
        txtStokSistem.setEditable(false);
        pOpname.add(txtStokSistem, gbc3);

        // 3. Physical Stock (Locked until Scan)
        gbc3.gridx = 0;
        gbc3.gridy = 2;
        gbc3.weightx = 0.3;
        pOpname.add(new JLabel("3. Stok Fisik (Input Manual):"), gbc3);

        gbc3.gridx = 1;
        gbc3.weightx = 0.7;
        txtStokFisik = createStyledField();
        txtStokFisik.setEnabled(false); // LOCKED INITIALLY
        txtStokFisik.setBackground(new Color(240, 240, 240));
        pOpname.add(txtStokFisik, gbc3);

        // Buttons
        JPanel p3Btn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p3Btn.setBackground(Color.WHITE);

        JButton btnResetOpname = new JButton("🔄 Reset");
        btnCekStok = new JButton("⚖️ BANDINGKAN (AUDIT)");
        btnCekStok.setEnabled(false); // LOCKED INITIALLY
        btnCekStok.setBackground(new Color(39, 174, 96));
        btnCekStok.setForeground(Color.WHITE);

        p3Btn.add(btnResetOpname);
        p3Btn.add(btnCekStok);

        btnResetOpname.addActionListener(e -> {
            txtScanStok.setText("");
            txtStokSistem.setText("");
            txtStokFisik.setText("");
            txtStokFisik.setEnabled(false);
            btnCekStok.setEnabled(false);
            txtScanStok.requestFocus();
        });

        btnCekStok.addActionListener(e -> handleCekStok());

        // Assembly
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        p3.add(pOpname, gbc);

        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        p3.add(p3Btn, gbc);

        tabbedPane.addTab("📊 Stok Opname", new JScrollPane(p3));

        // --- TAB 4: LAPORAN ---
        JPanel p4 = new JPanel(new GridBagLayout());
        p4.setBackground(new Color(245, 247, 250));

        JPanel pMenu = new JPanel(new GridLayout(3, 1, 20, 20));
        pMenu.setOpaque(false);

        JButton btnPrintKeuangan = createLargeButton("💰 CETAK LAPORAN KEUANGAN", "Laporan Pendapatan & HPP");
        JButton btnPrintBarang = createLargeButton("📦 CETAK LAPORAN STOK", "Mutasi Stok & Expired Date");
        JButton btnPrintKlinis = createLargeButton("🩺 CETAK DATA KLINIS", "Riwayat Resep & Pasien");

        btnPrintKeuangan.addActionListener(e -> cetakLaporan("Laporan_Keuangan.pdf", 1));
        btnPrintBarang.addActionListener(e -> cetakLaporan("Laporan_Barang.pdf", 2));
        btnPrintKlinis.addActionListener(e -> cetakLaporan("Laporan_Klinis.pdf", 3));

        pMenu.add(btnPrintKeuangan);
        pMenu.add(btnPrintBarang);
        pMenu.add(btnPrintKlinis);
        p4.add(pMenu);

        tabbedPane.addTab("📑 Laporan", p4);

        add(tabbedPane, BorderLayout.CENTER);

        txtResult = new JTextArea(8, 60);
        txtResult.setEditable(false);
        txtResult.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtResult.setBackground(new Color(44, 62, 80));
        txtResult.setForeground(new Color(236, 240, 241));

        JPanel pLog = new JPanel(new BorderLayout());
        pLog.setBorder(BorderFactory.createTitledBorder("System Log / Validation Output"));
        pLog.add(new JScrollPane(txtResult), BorderLayout.CENTER);
        add(pLog, BorderLayout.SOUTH);
    }

    // --- UI HELPER METHODS ---
    private javax.swing.border.Border createModernBorder(String title) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(), title,
                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                        javax.swing.border.TitledBorder.DEFAULT_POSITION,
                        new Font("Segoe UI", Font.BOLD, 14), new Color(44, 62, 80)));
    }

    private void addFormRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(l, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        p.add(comp, gbc);
    }

    private void addLabelAndField(JPanel p, String label, JTextField field) {
        p.add(new JLabel(label));
        p.add(field);
    }

    private JTextField createStyledField() {
        return createStyledField("");
    }

    private JTextField createStyledField(String text) {
        JTextField tf = new JTextField(text);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return tf;
    }

    private JButton createLargeButton(String title, String subtitle) {
        JButton btn = new JButton("<html><center><b style='font-size:14px'>" + title
                + "</b><br><span style='font-size:10px'>" + subtitle + "</span></center></html>");
        btn.setPreferredSize(new Dimension(300, 80));
        btn.setFocusPainted(false);
        return btn;
    }

    // --- LOGIC ---
    private void handleSimpanBarang() {
        try (Connection conn = getConnection()) {
            ensureTableExists(conn);
            String nama = txtNamaObat.getText();
            String batch = txtBatch.getText();
            int qtyInput = 0;
            try {
                qtyInput = Integer.parseInt(txtQty.getText());
            } catch (Exception e) {
            }

            String checkSql = "SELECT id, quantity FROM obat WHERE LOWER(nama_obat) = LOWER(?) AND LOWER(nomor_batch) = LOWER(?)";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, nama);
            checkStmt.setString(2, batch);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                int oldQty = rs.getInt("quantity");
                int newQty = oldQty + qtyInput; // Define newQty here
                String updateSql = "UPDATE obat SET quantity = ? WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, newQty);
                updateStmt.setInt(2, id);
                updateStmt.executeUpdate();
                log("✅ [UPDATE STOK] Batch " + batch + " ditemukan. Stok " + oldQty + " -> " + newQty);
            } else {
                // Fix: Use scanned/typed barcode if available, otherwise generate random
                String inputBarcode = txtScanBarcode.getText().trim();
                String barcode;
                if (inputBarcode != null && !inputBarcode.isEmpty()) {
                    barcode = inputBarcode;
                } else {
                    barcode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                }

                String insertSql = "INSERT INTO obat (nama_obat, nomor_batch, tgl_produksi, tgl_expired, supplier, harga_beli, quantity, barcode) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertSql);
                pstmt.setString(1, nama);
                pstmt.setString(2, batch);
                pstmt.setDate(3, java.sql.Date.valueOf(txtTglProd.getText()));
                pstmt.setDate(4, java.sql.Date.valueOf(txtExp.getText()));
                pstmt.setString(5, txtSupplier.getText());
                pstmt.setDouble(6, Double.parseDouble(txtHarga.getText()));
                pstmt.setInt(7, qtyInput);
                pstmt.setString(8, barcode);
                pstmt.setString(8, barcode);
                pstmt.executeUpdate();
                log("✅ Barang Baru Saved. Barcode: " + barcode);
            }

            // 2. Persistent Logging (Riwayat Barang)
            String logSql = "INSERT INTO riwayat_barang (nama_obat, nomor_batch, tipe, quantity, timestamp) VALUES (?, ?, 'MASUK', ?, CURRENT_TIMESTAMP)";
            PreparedStatement logStmt = conn.prepareStatement(logSql);
            logStmt.setString(1, nama);
            logStmt.setString(2, batch);
            logStmt.setInt(3, qtyInput);
            logStmt.executeUpdate();
        } catch (Exception ex) {
            log("❌ Error Simpan: " + ex.getMessage());
        }
    }

    private void handleAnalisisResep() {
        txtResult.setText("");
        log("🚀 Analyzing Prescription (CDSS)...");
        String obat = (String) cbObatResep.getSelectedItem();
        double dosis = 0, berat = 0, gfr = 120;
        int usia = 0;
        try {
            dosis = Double.parseDouble(txtDosis.getText());
            berat = Double.parseDouble(txtBB.getText());
            usia = Integer.parseInt(txtUsia.getText());
            if (!txtGFR.getText().isEmpty())
                gfr = Double.parseDouble(txtGFR.getText());
        } catch (NumberFormatException e) {
            log("❌ Invalid Number Format");
            return;
        }

        boolean aman = true;
        // Rules
        String alergi = (String) cbAlergi.getSelectedItem();
        if ("Antibiotik".equals(alergi) && (obat.equals("Amoxicillin") || obat.equals("Cefadroxil"))) {
            log("⛔ ALERGI ANTIBIOTIK! Jangan beri " + obat);
            aman = false;
        }
        if (gfr < 60 && (obat.equals("Ketorolac") || obat.equals("Asam Mefenamat"))) {
            log("⚠️ Ginjal Bermasalah (GFR " + gfr + "). Hindari NSAID.");
            aman = false;
        }

        String medikasiLain = txtMedikasiLain.getText().toLowerCase();
        if (medikasiLain.contains("warfarin") && (obat.equals("Ketorolac") || obat.equals("Asam Mefenamat"))) {
            log("❌ BAHAYA: Interaksi Warfarin + NSAID (Pendarahan!)");
            aman = false;
        }

        if (aman && cekStokDatabase(obat, 1)) {
            log("✅ Resep Valid & Aman. Silakan Dispense.");
            btnDispense.setEnabled(true);
        } else {
            log("❌ Resep Tidak Aman / Stok Habis.");
            btnDispense.setEnabled(false);
        }
    }

    private void handleDispense() {
        String obat = (String) cbObatResep.getSelectedItem();
        String pin = JOptionPane.showInputDialog("PIN Apoteker:");
        if (pin == null)
            return;

        try (Connection conn = getConnection()) {
            String sql = "UPDATE obat SET quantity = quantity - 1 WHERE nama_obat = ? AND quantity > 0 LIMIT 1";
            PreparedStatement st = conn.prepareStatement(sql);
            st.setString(1, obat);
            int aff = st.executeUpdate();
            if (aff > 0) {
                // 3. Simpan Riwayat Klinis
                String logSql = "INSERT INTO riwayat_klinis (nik, nama_pasien, diagnosa, nama_obat, dosis, tgl_kunjungan) VALUES (?, ?, ?, ?, ?, CURRENT_DATE)";
                PreparedStatement pstmt = conn.prepareStatement(logSql);
                pstmt.setString(1, txtNik.getText());
                pstmt.setString(2, txtNamaPasien.getText());
                pstmt.setString(3, txtDiagnosa.getText());
                pstmt.setString(4, obat);
                pstmt.setString(5, txtDosis.getText());
                pstmt.executeUpdate();

                log("✅ Dispensing Succesful: " + obat);
                btnDispense.setEnabled(false);
            } else {
                JOptionPane.showMessageDialog(this, "Stok Habis!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleLoadStok() {
        // Just hint user to scan
        JOptionPane.showMessageDialog(this, "Scan an Item Barcode to load System Stock!");
        txtScanStok.requestFocus();
    }

    private void handleCekStok() {
        try {
            int sys = Integer.parseInt(txtStokSistem.getText());
            int phy = Integer.parseInt(txtStokFisik.getText());
            int diff = phy - sys;
            log("📊 Stock Audit | Sys: " + sys + " | Phy: " + phy + " | Diff: " + diff);
        } catch (Exception e) {
            log("❌ Invalid Input");
        }
    }

    private void handleScanBarcodeAlur1() {
        String bc = txtScanBarcode.getText();
        try (Connection c = getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM obat WHERE barcode=?");
            ps.setString(1, bc);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtNamaObat.setText(rs.getString("nama_obat"));
                txtBatch.setText(rs.getString("nomor_batch"));
                txtSupplier.setText(rs.getString("supplier"));
                txtHarga.setText(String.valueOf(rs.getDouble("harga_beli")));
                log("🔍 Item Found: " + rs.getString("nama_obat"));
                txtQty.requestFocus();
            } else {
                log("ℹ️ New Item. Please fill details.");
                txtNamaObat.requestFocus();
            }
        } catch (Exception e) {
        }
    }

    private void handleScanResepOCR() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            // Mock OCR
            new SwingWorker<Void, Void>() {
                protected Void doInBackground() throws Exception {
                    Thread.sleep(1000);
                    return null;
                }

                protected void done() {
                    txtNamaPasien.setText("Budi (AI Extracted)");
                    txtDiagnosa.setText("ISPA (AI)");
                    cbObatResep.setSelectedItem("Amoxicillin");
                    txtDosis.setText("500");
                    log("✅ OCR Completed.");
                }
            }.execute();
        }
    }

    private void handleScanStokOpname() {
        String bc = txtScanStok.getText().trim();
        if (bc.isEmpty())
            return;

        try (Connection c = getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT nama_obat, quantity FROM obat WHERE barcode=?");
            ps.setString(1, bc);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String nama = rs.getString("nama_obat");
                int qty = rs.getInt("quantity");

                txtStokSistem.setText(String.valueOf(qty));

                // UNLOCK NEXT STEPS
                txtStokFisik.setEnabled(true);
                txtStokFisik.setBackground(Color.WHITE);
                txtStokFisik.requestFocus();
                btnCekStok.setEnabled(true);

                log("🔍 Item Found: " + nama + " | System Stock: " + qty);
            } else {
                txtStokSistem.setText("NOT FOUND");
                txtStokFisik.setEnabled(false);
                btnCekStok.setEnabled(false);
                log("❌ Barcode not found in database.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean cekStokDatabase(String nama, int qty) {
        try (Connection c = getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT SUM(quantity) FROM obat WHERE nama_obat=?");
            ps.setString(1, nama);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1) >= qty;
        } catch (Exception e) {
        }
        return false;
    }

    private void cetakLaporan(String filename, int type) {
        Document document = new Document(PageSize.A4.rotate()); // Landscape layout
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            // Header
            com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            String title = (type == 1) ? "LAPORAN KEUANGAN & LALU LINTAS BARANG"
                    : (type == 2) ? "LAPORAN STOK & INVENTORY" : "LAPORAN DATA PASIEN & KLINIS";

            document.add(new Paragraph("MY APOTEK PROFESSIONAL SYSTEM", titleFont));
            document.add(new Paragraph(title));
            document.add(new Paragraph("Generated: " + LocalDate.now() + "\n\n"));

            try (Connection conn = getConnection()) {
                PdfPTable table = null;

                if (type == 1) {
                    // 1. Laporan Transaksi (Riwayat Barang)
                    table = new PdfPTable(5);
                    addHeader(table, "ID", "Nama Barang", "Batch", "Tipe", "Qty");
                    ResultSet rs = conn.createStatement()
                            .executeQuery("SELECT * FROM riwayat_barang ORDER BY timestamp DESC");
                    while (rs.next()) {
                        table.addCell(String.valueOf(rs.getInt("id")));
                        table.addCell(rs.getString("nama_obat"));
                        table.addCell(rs.getString("nomor_batch"));
                        table.addCell(rs.getString("tipe"));
                        table.addCell(String.valueOf(rs.getInt("quantity")));
                    }
                } else if (type == 2) {
                    // 2. Laporan Stok (Inventory)
                    table = new PdfPTable(7);
                    addHeader(table, "Nama Obat", "Batch", "Exp Date", "Supplier", "Harga Beli", "Stok", "Barcode");
                    ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM obat ORDER BY nama_obat ASC");
                    while (rs.next()) {
                        table.addCell(rs.getString("nama_obat"));
                        table.addCell(rs.getString("nomor_batch"));
                        table.addCell(rs.getString("tgl_expired"));
                        table.addCell(rs.getString("supplier"));
                        table.addCell(String.valueOf(rs.getDouble("harga_beli")));
                        table.addCell(String.valueOf(rs.getInt("quantity")));
                        table.addCell(rs.getString("barcode"));
                    }
                } else if (type == 3) {
                    // 3. Laporan Klinis
                    table = new PdfPTable(6);
                    addHeader(table, "Tgl", "Pasien", "Diagnosa", "Obat", "Dosis", "NIK");
                    ResultSet rs = conn.createStatement()
                            .executeQuery("SELECT * FROM riwayat_klinis ORDER BY tgl_kunjungan DESC");
                    while (rs.next()) {
                        table.addCell(rs.getString("tgl_kunjungan"));
                        table.addCell(rs.getString("nama_pasien"));
                        table.addCell(rs.getString("diagnosa"));
                        table.addCell(rs.getString("nama_obat"));
                        table.addCell(rs.getString("dosis"));
                        table.addCell(rs.getString("nik"));
                    }
                }

                if (table != null) {
                    table.setWidthPercentage(100);
                    document.add(table);
                } else {
                    document.add(new Paragraph("No data found for this report type."));
                }
            }

            document.close();
            log("📄 PDF Created: " + filename);
            if (Desktop.isDesktopSupported())
                Desktop.getDesktop().open(new File(filename));
        } catch (Exception e) {
            log("❌ PDF Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addHeader(PdfPTable table, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            cell.setBackgroundColor(Color.LIGHT_GRAY);
            table.addCell(cell);
        }
    }

    private void ensureTableExists(Connection conn) throws SQLException {
        conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS obat (id INT AUTO_INCREMENT PRIMARY KEY, nama_obat VARCHAR(255), nomor_batch VARCHAR(100), tgl_produksi DATE, tgl_expired DATE, supplier VARCHAR(100), harga_beli DOUBLE, quantity INT, barcode VARCHAR(50))");
        conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS riwayat_klinis (id INT AUTO_INCREMENT PRIMARY KEY, nik VARCHAR(50), nama_pasien VARCHAR(100), diagnosa VARCHAR(100), nama_obat VARCHAR(100), dosis VARCHAR(50), tgl_kunjungan DATE)");
        conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS riwayat_barang (id INT AUTO_INCREMENT PRIMARY KEY, nama_obat VARCHAR(100), nomor_batch VARCHAR(100), tipe VARCHAR(20), quantity INT, timestamp TIMESTAMP)");
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:file:./data/my_apotek;AUTO_SERVER=TRUE", "sa", "");
    }

    private void log(String msg) {
        txtResult.append(msg + "\n");
        txtResult.setCaretPosition(txtResult.getDocument().getLength());
    }
}