package com.example.My.Apotek.controller;

import com.example.My.Apotek.model.Obat;
import com.example.My.Apotek.model.RiwayatKlinis;
import com.example.My.Apotek.service.ApotekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class WebController {

    @Autowired
    private ApotekService apotekService;

    @GetMapping("/")
    public String dashboard(Model model) {
        List<Obat> allObat = apotekService.getAllObat();
        List<RiwayatKlinis> allRiwayat = apotekService.getAllRiwayatKlinis();
        LocalDate today = LocalDate.now();

        int totalObat = allObat.stream().mapToInt(Obat::getQuantity).sum();
        long transaksiHariIni = allRiwayat.stream()
                .filter(r -> r.getTglKunjungan().isEqual(today))
                .count();
        long peringatanKritis = allObat.stream()
                .filter(o -> o.getQuantity() <= 10)
                .count();

        model.addAttribute("totalObat", totalObat);
        model.addAttribute("transaksiHariIni", transaksiHariIni);
        model.addAttribute("peringatanKritis", peringatanKritis);
        model.addAttribute("recentActivities",
                allRiwayat.size() > 5 ? allRiwayat.subList(allRiwayat.size() - 5, allRiwayat.size()) : allRiwayat);

        model.addAttribute("pageTitle", "Beranda");
        return "dashboard";
    }

    // --- INVENTORY ---
    @GetMapping("/inventory")
    public String inventory(Model model) {
        model.addAttribute("pageTitle", "Manajemen Stok");
        model.addAttribute("obatList", apotekService.getAllObat());
        model.addAttribute("newObat", new Obat());
        return "inventory";
    }

    @PostMapping("/inventory/save")
    public String saveObat(@ModelAttribute Obat obat, RedirectAttributes ra) {
        apotekService.tambahStokBarang(obat);
        ra.addFlashAttribute("message", "✅ Barang berhasil disimpan!");
        return "redirect:/inventory";
    }

    // --- PRESCRIPTION (ALUR 2) ---
    @GetMapping("/prescription")
    public String prescription(Model model) {
        model.addAttribute("pageTitle", "Layanan Pasien");
        model.addAttribute("resep", new RiwayatKlinis());
        model.addAttribute("obatList", apotekService.getAllObat());
        return "prescription";
    }

    @PostMapping("/prescription/check")
    public String checkPrescription(@ModelAttribute RiwayatKlinis resep,
            @RequestParam String riwayatAlergi,
            @RequestParam(required = false) Double gfr,
            @RequestParam(required = false) String medikasiLain,
            @RequestParam(required = false) boolean confirm,
            Model model) {

        // 1. Run CDSS Rules
        List<String> warnings = apotekService.validsaiResep(resep, riwayatAlergi, gfr, medikasiLain);

        // 2. If not confirmed yet, just show results
        if (!confirm) {
            model.addAttribute("warnings", warnings);
            if (warnings.isEmpty()) {
                model.addAttribute("message", "✅ Klinis Aman: Tidak ditemukan interaksi atau kontraindikasi.");
                model.addAttribute("status", "success");
            } else {
                model.addAttribute("status", "danger");
            }
            model.addAttribute("showConfirm", true);
            model.addAttribute("resep", resep);
            model.addAttribute("riwayatAlergi", riwayatAlergi);
            model.addAttribute("gfr", gfr);
            model.addAttribute("medikasiLain", medikasiLain);
            model.addAttribute("obatList", apotekService.getAllObat());
            model.addAttribute("pageTitle", "Layanan Pasien");
            return "prescription";
        }

        // 3. Process Dispense (If confirmed)
        boolean success = apotekService.processDispense(resep);
        if (success) {
            model.addAttribute("message", "✅ Resep Berhasil Diproses & Stok Dikurangi.");
            model.addAttribute("status", "success");
            // Reset form
            model.addAttribute("resep", new RiwayatKlinis());
        } else {
            model.addAttribute("warnings", List.of("❌ Gagal: Stok Obat Habis atau Tidak Ditemukan!"));
            model.addAttribute("status", "danger");
            model.addAttribute("resep", resep);
        }

        model.addAttribute("pageTitle", "Layanan Pasien");
        model.addAttribute("obatList", apotekService.getAllObat());
        return "prescription";
    }

    // --- AUDIT (STOK OPNAME) ---
    @GetMapping("/audit")
    public String audit(Model model) {
        model.addAttribute("pageTitle", "Audit Stok Opname");
        return "audit";
    }

    @PostMapping("/audit/check")
    public String runAudit(@RequestParam String barcode,
            @RequestParam(required = false) Integer fisik,
            Model model) {

        Obat target = apotekService.findObatByBarcode(barcode).orElse(null);
        if (target == null) {
            model.addAttribute("error", "❌ Barcode tidak ditemukan di database!");
        } else {
            model.addAttribute("item", target);
            if (fisik != null) {
                String result = apotekService.compareAudit(target.getQuantity(), fisik);
                model.addAttribute("auditResult", result);
                model.addAttribute("fisik", fisik);
            }
        }

        model.addAttribute("pageTitle", "Audit Stok Opname");
        return "audit";
    }

    // --- REPORTS ---
    @GetMapping("/reports")
    public String reports(Model model) {
        List<Obat> obatList = apotekService.getAllObat();
        List<RiwayatKlinis> riwayatList = apotekService.getAllRiwayatKlinis();

        // Calculate Revenue and Basic Financials
        double totalRevenue = riwayatList.stream()
                .mapToDouble(r -> {
                    return obatList.stream()
                            .filter(o -> o.getNamaObat().equalsIgnoreCase(r.getNamaObat()))
                            .findFirst()
                            .map(o -> o.getHargaBeli() * 1.3) // 30% Margin assume
                            .orElse(0.0);
                }).sum();

        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalMargin", totalRevenue * 0.23); // Estimated net margin
        model.addAttribute("totalItems", obatList.size());
        model.addAttribute("riwayatBarang", apotekService.getRiwayatBarang());
        model.addAttribute("riwayatKlinis", riwayatList);
        model.addAttribute("obatList", obatList);
        model.addAttribute("pageTitle", "Laporan Sistem");
        return "reports";
    }

    @GetMapping("/reports/export/pdf")
    public void exportToPDF(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Laporan_Apotek_" + LocalDate.now() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<Obat> obatList = apotekService.getAllObat();
        List<RiwayatKlinis> riwayatList = apotekService.getAllRiwayatKlinis();

        com.lowagie.text.Document document = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4);
        com.lowagie.text.pdf.PdfWriter.getInstance(document, response.getOutputStream());

        document.open();
        com.lowagie.text.Font fontTitle = com.lowagie.text.FontFactory
                .getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD);
        fontTitle.setSize(18);

        com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph("LAPORAN KEUANGAN & OPERASIONAL APOTEK",
                fontTitle);
        title.setAlignment(com.lowagie.text.Paragraph.ALIGN_CENTER);
        document.add(title);

        document.add(new com.lowagie.text.Paragraph(" "));
        document.add(new com.lowagie.text.Paragraph("Tanggal Laporan: " + LocalDate.now()));
        document.add(new com.lowagie.text.Paragraph(" "));

        // 1. Ringkasan Keuangan
        document.add(new com.lowagie.text.Paragraph("1. RINGKASAN KEUANGAN",
                com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD)));
        double rev = riwayatList.stream()
                .mapToDouble(r -> obatList.stream().filter(o -> o.getNamaObat().equalsIgnoreCase(r.getNamaObat()))
                        .findFirst().map(o -> o.getHargaBeli() * 1.3).orElse(0.0))
                .sum();
        document.add(new com.lowagie.text.Paragraph("Total Pendapatan: Rp " + String.format("%,.2f", rev)));
        document.add(new com.lowagie.text.Paragraph("Estimasi Margin (30%): Rp " + String.format("%,.2f", rev * 0.3)));
        document.add(new com.lowagie.text.Paragraph(" "));

        // 2. Daftar Stok
        document.add(new com.lowagie.text.Paragraph("2. STATUS STOK BARANG",
                com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD)));
        document.add(new com.lowagie.text.Paragraph(" "));
        com.lowagie.text.pdf.PdfPTable tableStok = new com.lowagie.text.pdf.PdfPTable(4);
        tableStok.setWidthPercentage(100);
        tableStok.addCell("Nama Obat");
        tableStok.addCell("SKU");
        tableStok.addCell("Qty");
        tableStok.addCell("Status");

        for (Obat o : obatList) {
            tableStok.addCell(o.getNamaObat());
            tableStok.addCell(o.getBarcode() != null ? o.getBarcode() : "-");
            tableStok.addCell(String.valueOf(o.getQuantity()));
            tableStok.addCell(o.getQuantity() <= 10 ? "KRITIS" : "AMAN");
        }
        document.add(tableStok);
        document.add(new com.lowagie.text.Paragraph(" "));

        // 3. Log Transaksi (Ringkasan)
        document.add(new com.lowagie.text.Paragraph("3. RINGKASAN LAYANAN KLINIS (CDSS)",
                com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD)));
        document.add(new com.lowagie.text.Paragraph("Total Resep Diproses: " + riwayatList.size()));
        document.add(new com.lowagie.text.Paragraph(" "));

        com.lowagie.text.pdf.PdfPTable tableKlinis = new com.lowagie.text.pdf.PdfPTable(3);
        tableKlinis.setWidthPercentage(100);
        tableKlinis.addCell("Tanggal");
        tableKlinis.addCell("Pasien");
        tableKlinis.addCell("Terapi Obat");

        for (RiwayatKlinis r : riwayatList) {
            tableKlinis.addCell(r.getTglKunjungan().toString());
            tableKlinis.addCell(r.getNamaPasien());
            tableKlinis.addCell(r.getNamaObat());
        }
        document.add(tableKlinis);

        document.add(new com.lowagie.text.Paragraph(" "));
        document.add(new com.lowagie.text.Paragraph("--- Akhir Laporan Profesional ---", com.lowagie.text.FontFactory
                .getFont(com.lowagie.text.FontFactory.HELVETICA, 10, com.lowagie.text.Font.ITALIC)));

        document.close();
    }
}
