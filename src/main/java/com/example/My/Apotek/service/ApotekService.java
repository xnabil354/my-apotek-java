package com.example.My.Apotek.service;

import com.example.My.Apotek.model.Obat;
import com.example.My.Apotek.model.RiwayatBarang;
import com.example.My.Apotek.model.RiwayatKlinis;
import com.example.My.Apotek.repository.ObatRepository;
import com.example.My.Apotek.repository.RiwayatBarangRepository;
import com.example.My.Apotek.repository.RiwayatKlinisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    // --- INVENTORY LOGIC ---

    public List<Obat> getAllObat() {
        return obatRepository.findAll();
    }

    public Optional<Obat> findObatByBarcode(String barcode) {
        return obatRepository.findByBarcode(barcode);
    }

    @Transactional
    public void tambahStokBarang(Obat input) {
        // Cek existing by Barcode or Name
        Optional<Obat> existing = obatRepository.findByBarcode(input.getBarcode());
        if (existing.isEmpty()) {
            // Fallback check by name if barcode logic allows
            // For now assume barcode is unique key
            obatRepository.save(input);
        } else {
            Obat obat = existing.get();
            obat.setQuantity(obat.getQuantity() + input.getQuantity());
            obatRepository.save(obat);
        }

        // Log Riwayat
        RiwayatBarang log = new RiwayatBarang();
        log.setNamaObat(input.getNamaObat());
        log.setNomorBatch(input.getNomorBatch());
        log.setTipe("MASUK");
        log.setQuantity(input.getQuantity());
        riwayatBarangRepository.save(log);
    }

    // --- CDSS LOGIC (RULES BASED LEARNING) ---

    public List<String> validsaiResep(RiwayatKlinis data, String riwayatAlergi, Double gfr, String medikasiLain) {
        List<String> warnings = new ArrayList<>();
        String obat = data.getNamaObat();

        // Rule 1: Alergi
        if ("Antibiotik".equalsIgnoreCase(riwayatAlergi) &&
                (obat.equalsIgnoreCase("Amoxicillin") || obat.equalsIgnoreCase("Cefadroxil"))) {
            warnings.add("⛔ ALERGI ANTIBIOTIK! Pasien alergi antibiotik, jangan berikan " + obat);
        }

        // Rule 2: Ginjal (GFR)
        if (gfr != null && gfr < 60 &&
                (obat.equalsIgnoreCase("Ketorolac") || obat.equalsIgnoreCase("Asam Mefenamat"))) {
            warnings.add("⚠️ GAGAL GINJAL (GFR Rendah). Hindari NSAID (" + obat + ")");
        }

        // Rule 3: Interaksi Obat
        if (medikasiLain != null && medikasiLain.toLowerCase().contains("warfarin") &&
                (obat.equalsIgnoreCase("Ketorolac") || obat.equalsIgnoreCase("Asam Mefenamat"))) {
            warnings.add("❌ BAHAYA INTEGRASI: Warfarin + NSAID menyebabkan risiko pendarahan tinggi!");
        }

        return warnings;
    }

    @Transactional
    public boolean processDispense(RiwayatKlinis resep) {
        // 1. Cek Stok
        // Note: In real logic, we might need accurate lookup by exact name
        // Here we just scan all matches roughly or exact match
        List<Obat> stokList = obatRepository.findAll().stream()
                .filter(o -> o.getNamaObat().equalsIgnoreCase(resep.getNamaObat()))
                .toList();

        if (stokList.isEmpty())
            return false; // Not found

        Obat target = stokList.get(0); // Take first batch found
        if (target.getQuantity() < 1)
            return false; // Empty

        // 2. Reduce Stock
        target.setQuantity(target.getQuantity() - 1);
        obatRepository.save(target);

        // 3. Save History
        riwayatKlinisRepository.save(resep);

        return true;
    }
    // --- REPORTING LOGIC ---

    public List<RiwayatBarang> getRiwayatBarang() {
        return riwayatBarangRepository.findAll();
    }

    public List<RiwayatKlinis> getRiwayatKlinis() {
        return riwayatKlinisRepository.findAll();
    }

    // --- AUDIT LOGIC ---

    public String compareAudit(Integer sistem, Integer fisik) {
        if (sistem == null || fisik == null)
            return "Data tidak valid";
        int diff = fisik - sistem;
        double tolerance = sistem * 0.05; // 5% Tolerance rule

        if (Math.abs(diff) > tolerance) {
            return "⚠️ SELISIH TINGGI (" + diff + "). Butuh Investigasi Apoteker.";
        } else {
            return "✅ SELISIH NORMAL (" + diff + "). Masuk batas toleransi 5%.";
        }
    }

    public List<RiwayatKlinis> getAllRiwayatKlinis() {
        return riwayatKlinisRepository.findAll();
    }
}
