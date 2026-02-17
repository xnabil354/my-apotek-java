package com.example.My.Apotek.service;

import com.example.My.Apotek.model.*;
import com.example.My.Apotek.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ApotekService {

    @Autowired
    private ObatRepository obatRepository;

    @Autowired
    private RiwayatBarangRepository riwayatBarangRepository;

    @Autowired
    private RiwayatKlinisRepository riwayatKlinisRepository;

    @Autowired
    private PenjualanRepository penjualanRepository;

    @Autowired
    private DetailPenjualanRepository detailPenjualanRepository;

    @Autowired
    private StokOpnameRepository stokOpnameRepository;

    public List<Obat> getAllObat() {
        return obatRepository.findAll();
    }

    public Optional<Obat> findObatByNoFaktur(String noFaktur) {
        return obatRepository.findByNoFaktur(noFaktur);
    }

    public Optional<Obat> findObatByNama(String nama) {
        List<Obat> list = obatRepository.findByNamaObat(nama);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<Obat> searchObat(String keyword) {
        return obatRepository.findByNamaObatContainingIgnoreCase(keyword);
    }

    @Transactional
    public void tambahStokBarang(Obat input) {
        Optional<Obat> existing = obatRepository.findByNoFaktur(input.getNoFaktur());
        if (existing.isEmpty()) {
            obatRepository.save(input);
        } else {
            Obat obat = existing.get();
            obat.setQuantity(obat.getQuantity() + input.getQuantity());
            if (input.getHargaBeli() != null)
                obat.setHargaBeli(input.getHargaBeli());
            if (input.getHargaJualApotek() != null)
                obat.setHargaJualApotek(input.getHargaJualApotek());
            if (input.getHargaPlot() != null)
                obat.setHargaPlot(input.getHargaPlot());
            if (input.getNamaPBF() != null)
                obat.setNamaPBF(input.getNamaPBF());
            if (input.getSatuan() != null)
                obat.setSatuan(input.getSatuan());
            if (input.getIndikasi() != null)
                obat.setIndikasi(input.getIndikasi());
            if (input.getGolongan() != null)
                obat.setGolongan(input.getGolongan());
            obatRepository.save(obat);
        }

        RiwayatBarang log = new RiwayatBarang();
        log.setNamaObat(input.getNamaObat());
        log.setNomorBatch(input.getNomorBatch());
        log.setTipe("MASUK");
        log.setQuantity(input.getQuantity());
        log.setNamaPBF(input.getNamaPBF());
        log.setHargaBeli(input.getHargaBeli());
        riwayatBarangRepository.save(log);
    }

    public List<String> validsaiResep(RiwayatKlinis data, String riwayatAlergi, Double gfr, String medikasiLain) {
        List<String> warnings = new ArrayList<>();
        String obat = data.getNamaObat();

        if (obat == null || obat.isBlank()) {
            warnings.add("Nama obat belum dipilih!");
            return warnings;
        }

        if (riwayatAlergi != null && "Antibiotik".equalsIgnoreCase(riwayatAlergi.trim()) &&
                (obat.equalsIgnoreCase("Amoxicillin") || obat.equalsIgnoreCase("Cefadroxil"))) {
            warnings.add("⛔ ALERGI ANTIBIOTIK! Pasien alergi antibiotik, jangan berikan " + obat);
        }

        if (gfr != null && gfr < 60 &&
                (obat.equalsIgnoreCase("Ketorolac") || obat.equalsIgnoreCase("Asam Mefenamat"))) {
            warnings.add("⚠️ GAGAL GINJAL (GFR Rendah). Hindari NSAID (" + obat + ")");
        }

        if (medikasiLain != null && medikasiLain.toLowerCase().contains("warfarin") &&
                (obat.equalsIgnoreCase("Ketorolac") || obat.equalsIgnoreCase("Asam Mefenamat"))) {
            warnings.add("❌ BAHAYA INTEGRASI: Warfarin + NSAID menyebabkan risiko pendarahan tinggi!");
        }

        return warnings;
    }

    @Transactional
    public boolean processDispense(RiwayatKlinis resep) {
        List<Obat> stokList = obatRepository.findAll().stream()
                .filter(o -> o.getNamaObat().equalsIgnoreCase(resep.getNamaObat()))
                .toList();

        if (stokList.isEmpty())
            return false;

        Obat target = stokList.get(0);
        int qty = (resep.getJumlahObat() != null && resep.getJumlahObat() > 0) ? resep.getJumlahObat() : 1;
        if (target.getQuantity() < qty)
            return false;

        target.setQuantity(target.getQuantity() - qty);
        obatRepository.save(target);

        if (resep.getHargaObat() == null && target.getHargaJualApotek() != null) {
            resep.setHargaObat(target.getHargaJualApotek());
        }
        if (resep.getJumlahObat() == null) {
            resep.setJumlahObat(qty);
        }

        riwayatKlinisRepository.save(resep);

        RiwayatBarang logKeluar = new RiwayatBarang();
        logKeluar.setNamaObat(resep.getNamaObat());
        logKeluar.setNomorBatch(target.getNomorBatch());
        logKeluar.setTipe("KELUAR");
        logKeluar.setQuantity(qty);
        riwayatBarangRepository.save(logKeluar);

        return true;
    }

    @Transactional
    public StokOpname performStokOpname(String noFaktur, int stokFisik) {
        Optional<Obat> opt = obatRepository.findByNoFaktur(noFaktur);
        if (opt.isEmpty())
            return null;

        Obat obat = opt.get();
        int sistem = obat.getQuantity();
        int selisih = stokFisik - sistem;
        double persen = sistem > 0 ? (Math.abs(selisih) * 100.0 / sistem) : (selisih == 0 ? 0 : 100);
        String status = (persen > 5.0 && Math.abs(selisih) > 1) ? "SELISIH TINGGI" : "NORMAL";

        StokOpname opname = new StokOpname();
        opname.setNamaObat(obat.getNamaObat());
        opname.setNoFaktur(noFaktur);
        opname.setStokSistem(sistem);
        opname.setStokFisik(stokFisik);
        opname.setSelisih(selisih);
        opname.setStatus(status);

        stokOpnameRepository.save(opname);

        if ("NORMAL".equals(status) && selisih != 0) {
            obat.setQuantity(stokFisik);
            obatRepository.save(obat);
        }

        return opname;
    }

    @Transactional
    public void adjustStok(String noFaktur, int stokFisik) {
        Optional<Obat> opt = obatRepository.findByNoFaktur(noFaktur);
        if (opt.isPresent()) {
            Obat obat = opt.get();
            obat.setQuantity(stokFisik);
            obatRepository.save(obat);
        }
    }

    public List<StokOpname> getHistoryOpname() {
        return stokOpnameRepository.findAllByOrderByTanggalDesc();
    }

    @Transactional
    public Penjualan createPenjualan(String namaPembeli, List<String> namaObatList, List<Integer> jumlahList,
            Double diskonPersen) {
        Penjualan penjualan = new Penjualan();
        penjualan.setNamaPembeli(namaPembeli);

        double total = 0;
        for (int i = 0; i < namaObatList.size(); i++) {
            String namaObat = namaObatList.get(i);
            int jumlah = jumlahList.get(i);

            List<Obat> matches = obatRepository.findByNamaObat(namaObat);
            if (matches.isEmpty())
                continue;
            Optional<Obat> opt = Optional.of(matches.get(0));

            Obat obat = opt.get();
            if (obat.getQuantity() < jumlah)
                continue;

            obat.setQuantity(obat.getQuantity() - jumlah);
            obatRepository.save(obat);

            DetailPenjualan detail = new DetailPenjualan();
            detail.setPenjualan(penjualan);
            detail.setNamaObat(namaObat);
            detail.setHargaJual(obat.getHargaJualApotek() != null ? obat.getHargaJualApotek() : obat.getHargaBeli());
            detail.setJumlah(jumlah);
            penjualan.getItems().add(detail);

            double subtotal = detail.getHargaJual() * jumlah;
            total += subtotal;

            RiwayatBarang logKeluar = new RiwayatBarang();
            logKeluar.setNamaObat(namaObat);
            logKeluar.setNomorBatch(obat.getNomorBatch());
            logKeluar.setTipe("KELUAR");
            logKeluar.setQuantity(jumlah);
            riwayatBarangRepository.save(logKeluar);
        }

        penjualan.setTotalHarga(total);

        if (diskonPersen != null && diskonPersen > 0) {
            penjualan.setDiskonPersen(diskonPersen);
            double diskonNominal = total * (diskonPersen / 100);
            penjualan.setDiskonNominal(diskonNominal);
            penjualan.setTotalSetelahDiskon(total - diskonNominal);
        } else {
            penjualan.setDiskonPersen(0.0);
            penjualan.setDiskonNominal(0.0);
            penjualan.setTotalSetelahDiskon(total);
        }

        return penjualanRepository.save(penjualan);
    }

    public List<Penjualan> getAllPenjualan() {
        return penjualanRepository.findAll();
    }

    public List<RiwayatBarang> getRiwayatBarang() {
        return riwayatBarangRepository.findAll();
    }

    public List<RiwayatKlinis> getRiwayatKlinis() {
        return riwayatKlinisRepository.findAll();
    }

    public List<RiwayatKlinis> getAllRiwayatKlinis() {
        return riwayatKlinisRepository.findAll();
    }

    public double getOmsetHarian(LocalDate date) {
        double omsetResep = riwayatKlinisRepository.findByTglKunjungan(date).stream()
                .mapToDouble(r -> r.getTotalHarga() != null ? r.getTotalHarga() : 0.0)
                .sum();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        double omsetPenjualan = penjualanRepository.findByTanggalBetween(startOfDay, endOfDay).stream()
                .mapToDouble(p -> p.getTotalHarga() != null ? p.getTotalHarga() : 0.0)
                .sum();
        return omsetResep + omsetPenjualan;
    }

    public double getOmsetBulanan(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        double omsetResep = riwayatKlinisRepository.findByTglKunjunganBetween(start, end).stream()
                .mapToDouble(r -> r.getTotalHarga() != null ? r.getTotalHarga() : 0.0)
                .sum();
        LocalDateTime startDT = start.atStartOfDay();
        LocalDateTime endDT = end.atTime(LocalTime.MAX);
        double omsetPenjualan = penjualanRepository.findByTanggalBetween(startDT, endDT).stream()
                .mapToDouble(p -> p.getTotalHarga() != null ? p.getTotalHarga() : 0.0)
                .sum();
        return omsetResep + omsetPenjualan;
    }

    public double getOmsetTahunan(int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        double omsetResep = riwayatKlinisRepository.findByTglKunjunganBetween(start, end).stream()
                .mapToDouble(r -> r.getTotalHarga() != null ? r.getTotalHarga() : 0.0)
                .sum();
        LocalDateTime startDT = start.atStartOfDay();
        LocalDateTime endDT = end.atTime(LocalTime.MAX);
        double omsetPenjualan = penjualanRepository.findByTanggalBetween(startDT, endDT).stream()
                .mapToDouble(p -> p.getTotalHarga() != null ? p.getTotalHarga() : 0.0)
                .sum();
        return omsetResep + omsetPenjualan;
    }

    public byte[] exportOmsetExcel(String periode, LocalDate startDate, LocalDate endDate) throws IOException {
        List<RiwayatKlinis> resepList = riwayatKlinisRepository.findByTglKunjunganBetween(startDate, endDate);
        LocalDateTime startDT = startDate.atStartOfDay();
        LocalDateTime endDT = endDate.atTime(LocalTime.MAX);
        List<Penjualan> penjualanList = penjualanRepository.findByTanggalBetween(startDT, endDT);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Sheet sheetResep = workbook.createSheet("Omset Resep");
            Row headerRow = sheetResep.createRow(0);
            String[] headersResep = { "No", "Tanggal", "Pasien", "Obat", "Qty", "Harga", "Tuslah", "Embalase",
                    "Total" };
            for (int i = 0; i < headersResep.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headersResep[i]);
                cell.setCellStyle(headerStyle);
            }
            double totalResep = 0;
            for (int i = 0; i < resepList.size(); i++) {
                RiwayatKlinis r = resepList.get(i);
                Row row = sheetResep.createRow(i + 1);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(r.getTglKunjungan() != null ? r.getTglKunjungan().toString() : "");
                row.createCell(2).setCellValue(r.getNamaPasien() != null ? r.getNamaPasien() : "");
                row.createCell(3).setCellValue(r.getNamaObat() != null ? r.getNamaObat() : "");
                row.createCell(4).setCellValue(r.getJumlahObat() != null ? r.getJumlahObat() : 1);
                row.createCell(5).setCellValue(r.getHargaObat() != null ? r.getHargaObat() : 0);
                row.createCell(6).setCellValue(r.getTuslah() != null ? r.getTuslah() : 0);
                row.createCell(7).setCellValue(r.getEmbalase() != null ? r.getEmbalase() : 0);
                row.createCell(8).setCellValue(r.getTotalHarga() != null ? r.getTotalHarga() : 0);
                totalResep += (r.getTotalHarga() != null ? r.getTotalHarga() : 0);
            }
            Row totalRow = sheetResep.createRow(resepList.size() + 1);
            totalRow.createCell(7).setCellValue("TOTAL:");
            totalRow.createCell(8).setCellValue(totalResep);
            for (int i = 0; i < headersResep.length; i++)
                sheetResep.autoSizeColumn(i);
            Sheet sheetPenjualan = workbook.createSheet("Omset Penjualan");
            Row headerRow2 = sheetPenjualan.createRow(0);
            String[] headersPenjualan = { "No", "No Transaksi", "Tanggal", "Pembeli", "Total" };
            for (int i = 0; i < headersPenjualan.length; i++) {
                Cell cell = headerRow2.createCell(i);
                cell.setCellValue(headersPenjualan[i]);
                cell.setCellStyle(headerStyle);
            }
            double totalPenjualan = 0;
            for (int i = 0; i < penjualanList.size(); i++) {
                Penjualan p = penjualanList.get(i);
                Row row = sheetPenjualan.createRow(i + 1);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(p.getNoTransaksi() != null ? p.getNoTransaksi() : "");
                row.createCell(2).setCellValue(p.getTanggal() != null ? p.getTanggal().toString() : "");
                row.createCell(3).setCellValue(p.getNamaPembeli() != null ? p.getNamaPembeli() : "");
                row.createCell(4).setCellValue(p.getTotalHarga() != null ? p.getTotalHarga() : 0);
                totalPenjualan += (p.getTotalHarga() != null ? p.getTotalHarga() : 0);
            }
            Row totalRow2 = sheetPenjualan.createRow(penjualanList.size() + 1);
            totalRow2.createCell(3).setCellValue("TOTAL:");
            totalRow2.createCell(4).setCellValue(totalPenjualan);
            for (int i = 0; i < headersPenjualan.length; i++)
                sheetPenjualan.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportPBFExcel() throws IOException {
        List<RiwayatBarang> allBarang = riwayatBarangRepository.findByTipe("MASUK");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Sheet sheet = workbook.createSheet("Laporan Input Obat PBF");
            Row headerRow = sheet.createRow(0);
            String[] headers = { "No", "Waktu", "Nama Obat", "Batch", "Nama PBF", "Qty", "Harga Beli", "Total" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            double grandTotal = 0;
            for (int i = 0; i < allBarang.size(); i++) {
                RiwayatBarang r = allBarang.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(r.getTimestamp() != null ? r.getTimestamp().toString() : "");
                row.createCell(2).setCellValue(r.getNamaObat() != null ? r.getNamaObat() : "");
                row.createCell(3).setCellValue(r.getNomorBatch() != null ? r.getNomorBatch() : "");
                row.createCell(4).setCellValue(r.getNamaPBF() != null ? r.getNamaPBF() : "-");
                row.createCell(5).setCellValue(r.getQuantity() != null ? r.getQuantity() : 0);
                row.createCell(6).setCellValue(r.getHargaBeli() != null ? r.getHargaBeli() : 0);
                row.createCell(7).setCellValue(r.getTotalHarga() != null ? r.getTotalHarga() : 0);
                grandTotal += (r.getTotalHarga() != null ? r.getTotalHarga() : 0);
            }

            Row totalRow = sheet.createRow(allBarang.size() + 1);
            totalRow.createCell(6).setCellValue("GRAND TOTAL:");
            totalRow.createCell(7).setCellValue(grandTotal);
            for (int i = 0; i < headers.length; i++)
                sheet.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public String compareAudit(Integer sistem, Integer fisik) {
        if (sistem == null || fisik == null)
            return "Data tidak valid";
        int diff = fisik - sistem;
        double persen = sistem > 0 ? (Math.abs(diff) * 100.0 / sistem) : (diff == 0 ? 0 : 100);

        if (persen > 5.0 && Math.abs(diff) > 1) {
            return "⚠️ SELISIH TINGGI (" + diff + " unit, " + String.format("%.1f", persen)
                    + "%). Butuh Approval Apoteker.";
        } else {
            return "✅ SELISIH NORMAL (" + diff + " unit, " + String.format("%.1f", persen)
                    + "%). Auto-adjust diterapkan.";
        }
    }
}
