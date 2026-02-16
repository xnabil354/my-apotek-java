package com.example.My.Apotek.controller;

import com.example.My.Apotek.model.*;
import com.example.My.Apotek.service.ApotekService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class WebController {

    @Autowired
    private ApotekService apotekService;

    @GetMapping("/")
    public String dashboard(Model model) {
        List<Obat> allObat = apotekService.getAllObat();
        model.addAttribute("totalObat", allObat.size());

        List<RiwayatKlinis> allKlinis = apotekService.getAllRiwayatKlinis();
        long todayTx = allKlinis.stream()
                .filter(r -> r.getTglKunjungan() != null && r.getTglKunjungan().equals(LocalDate.now()))
                .count();
        model.addAttribute("transaksiHariIni", todayTx);

        long critical = allObat.stream()
                .filter(o -> o.getQuantity() != null && o.getQuantity() < 10)
                .count();
        model.addAttribute("peringatanKritis", critical);

        List<RiwayatKlinis> recent = allKlinis.stream()
                .sorted((a, b) -> {
                    if (a.getTglKunjungan() == null)
                        return 1;
                    if (b.getTglKunjungan() == null)
                        return -1;
                    return b.getTglKunjungan().compareTo(a.getTglKunjungan());
                })
                .limit(10)
                .toList();
        model.addAttribute("recentActivities", recent);

        return "dashboard";
    }

    @GetMapping("/inventory")
    public String inventory(Model model) {
        model.addAttribute("obatList", apotekService.getAllObat());
        model.addAttribute("newObat", new Obat());
        return "inventory";
    }

    @PostMapping("/inventory/add")
    public String addObat(@ModelAttribute Obat obat, RedirectAttributes redirect) {
        try {
            apotekService.tambahStokBarang(obat);
            redirect.addFlashAttribute("success", "Obat berhasil ditambahkan!");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Gagal menambahkan obat: " + e.getMessage());
        }
        return "redirect:/inventory";
    }

    @GetMapping("/prescription")
    public String prescription(Model model) {
        model.addAttribute("obatList", apotekService.getAllObat());
        model.addAttribute("resep", new RiwayatKlinis());
        return "prescription";
    }

    @PostMapping("/prescription/check")
    public String checkPrescription(
            @ModelAttribute("resep") RiwayatKlinis resep,
            @RequestParam(value = "riwayatAlergi", required = false) String alergi,
            @RequestParam(value = "gfr", required = false) Double gfr,
            @RequestParam(value = "medikasiLain", required = false) String medikasiLain,
            Model model) {

        try {
            List<String> warnings = apotekService.validsaiResep(resep, alergi, gfr, medikasiLain);
            model.addAttribute("warnings", warnings);
            model.addAttribute("resep", resep);
            model.addAttribute("riwayatAlergi", alergi);
            model.addAttribute("gfr", gfr);
            model.addAttribute("medikasiLain", medikasiLain);
            model.addAttribute("obatList", apotekService.getAllObat());
            model.addAttribute("checked", true);
            if (resep.getNamaObat() != null && !resep.getNamaObat().isBlank()) {
                apotekService.findObatByNama(resep.getNamaObat()).ifPresent(obat -> {
                    if (resep.getHargaObat() == null && obat.getHargaJualApotek() != null) {
                        resep.setHargaObat(obat.getHargaJualApotek());
                    }
                });
            }
        } catch (Exception e) {
            model.addAttribute("error", "Gagal cek resep: " + e.getMessage());
            model.addAttribute("obatList", apotekService.getAllObat());
            model.addAttribute("resep", resep);
        }

        return "prescription";
    }

    @PostMapping("/prescription/confirm")
    public String confirmPrescription(
            @ModelAttribute("resep") RiwayatKlinis resep,
            RedirectAttributes redirect) {

        boolean success = apotekService.processDispense(resep);
        if (success) {
            redirect.addFlashAttribute("success", "Resep berhasil diproses dan obat telah diserahkan!");
        } else {
            redirect.addFlashAttribute("error", "Gagal memproses resep. Periksa ketersediaan stok!");
        }
        return "redirect:/prescription";
    }

    @GetMapping("/audit")
    public String audit(Model model) {
        model.addAttribute("opnameHistory", apotekService.getHistoryOpname());
        return "audit";
    }

    @PostMapping("/audit/check")
    public String auditCheck(
            @RequestParam("noFaktur") String noFaktur,
            @RequestParam(value = "fisik", required = false) Integer fisik,
            Model model) {

        var item = apotekService.findObatByNoFaktur(noFaktur);
        if (item.isEmpty()) {
            model.addAttribute("error", "Obat dengan No Faktur '" + noFaktur + "' tidak ditemukan.");
        } else {
            model.addAttribute("item", item.get());
            model.addAttribute("noFaktur", noFaktur);

            if (fisik != null) {
                String result = apotekService.compareAudit(item.get().getQuantity(), fisik);
                model.addAttribute("auditResult", result);
                model.addAttribute("fisik", fisik);

                StokOpname opname = apotekService.performStokOpname(noFaktur, fisik);
                if (opname != null) {
                    model.addAttribute("opnameRecord", opname);
                    model.addAttribute("opnameStatus", opname.getStatus());
                }
            }
        }
        model.addAttribute("opnameHistory", apotekService.getHistoryOpname());
        return "audit";
    }

    @PostMapping("/audit/adjust")
    public String auditAdjust(
            @RequestParam("noFaktur") String noFaktur,
            @RequestParam("fisik") int fisik,
            RedirectAttributes redirect) {
        apotekService.adjustStok(noFaktur, fisik);
        redirect.addFlashAttribute("success", "Stok berhasil disesuaikan ke " + fisik + " unit.");
        return "redirect:/audit";
    }

    @GetMapping("/penjualan")
    public String penjualan(Model model) {
        model.addAttribute("obatList", apotekService.getAllObat());
        model.addAttribute("penjualanList", apotekService.getAllPenjualan());
        return "penjualan";
    }

    @PostMapping("/penjualan/process")
    public String processPenjualan(
            @RequestParam("namaPembeli") String namaPembeli,
            @RequestParam("namaObat") List<String> namaObatList,
            @RequestParam("jumlah") List<Integer> jumlahList,
            RedirectAttributes redirect) {

        Penjualan result = apotekService.createPenjualan(namaPembeli, namaObatList, jumlahList);
        if (result != null && result.getId() != null) {
            redirect.addFlashAttribute("success",
                    "Penjualan berhasil! No Transaksi: " + result.getNoTransaksi() +
                            " | Total: Rp " + String.format("%,.0f", result.getTotalHarga()));
        } else {
            redirect.addFlashAttribute("error", "Gagal memproses penjualan. Periksa stok!");
        }
        return "redirect:/penjualan";
    }

    @GetMapping("/mims")
    public String mims(
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {

        List<Obat> obatList;
        if (keyword != null && !keyword.isBlank()) {
            obatList = apotekService.searchObat(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            obatList = apotekService.getAllObat();
        }
        model.addAttribute("obatList", obatList);
        return "mims";
    }

    @GetMapping("/reports")
    public String reports(
            @RequestParam(value = "tanggalHarian", required = false) String tanggalHarian,
            @RequestParam(value = "bulan", required = false) Integer bulan,
            @RequestParam(value = "tahunBulanan", required = false) Integer tahunBulanan,
            @RequestParam(value = "tahunTahunan", required = false) Integer tahunTahunan,
            Model model) {

        model.addAttribute("riwayatBarang", apotekService.getRiwayatBarang());
        model.addAttribute("riwayatKlinis", apotekService.getRiwayatKlinis());

        List<Obat> allObat = apotekService.getAllObat();
        double totalRevenue = allObat.stream()
                .mapToDouble(o -> {
                    double hj = o.getHargaJualApotek() != null ? o.getHargaJualApotek() : 0;
                    double hb = o.getHargaBeli() != null ? o.getHargaBeli() : 0;
                    int qty = o.getQuantity() != null ? o.getQuantity() : 0;
                    return (hj - hb) * qty;
                })
                .sum();
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalMargin", totalRevenue);
        model.addAttribute("activeItems",
                allObat.stream().filter(o -> o.getQuantity() != null && o.getQuantity() > 0).count());
        if (tanggalHarian != null && !tanggalHarian.isBlank()) {
            LocalDate dateH = LocalDate.parse(tanggalHarian);
            model.addAttribute("omsetHarian", apotekService.getOmsetHarian(dateH));
            model.addAttribute("tanggalHarian", tanggalHarian);
        }
        if (bulan != null && tahunBulanan != null) {
            model.addAttribute("omsetBulanan", apotekService.getOmsetBulanan(tahunBulanan, bulan));
            model.addAttribute("bulan", bulan);
            model.addAttribute("tahunBulanan", tahunBulanan);
        }
        if (tahunTahunan != null) {
            model.addAttribute("omsetTahunan", apotekService.getOmsetTahunan(tahunTahunan));
            model.addAttribute("tahunTahunan", tahunTahunan);
        }

        return "reports";
    }

    @GetMapping("/reports/export/omset")
    public void exportOmset(
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr,
            HttpServletResponse response) throws IOException {

        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        byte[] data = apotekService.exportOmsetExcel("Custom", startDate, endDate);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=Laporan_Omset_" + startDateStr + "_" + endDateStr + ".xlsx");
        response.getOutputStream().write(data);
        response.getOutputStream().flush();
    }

    @GetMapping("/reports/export/pbf")
    public void exportPBF(HttpServletResponse response) throws IOException {
        byte[] data = apotekService.exportPBFExcel();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Laporan_Input_PBF.xlsx");
        response.getOutputStream().write(data);
        response.getOutputStream().flush();
    }
}
