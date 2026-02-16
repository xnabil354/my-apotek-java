package com.example.My.Apotek.desktop;

import com.example.My.Apotek.model.*;
import com.example.My.Apotek.service.ApotekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApotekController {

    @Autowired
    private ApotekService apotekService;

    @PostMapping("/medicine")
    public ResponseEntity<?> addMedicine(@RequestBody Medicine med) {
        Obat obat = new Obat();
        obat.setNamaObat(med.getNamaObat());
        obat.setNomorBatch(med.getNomorBatch());
        obat.setNoFaktur(med.getNoFaktur());
        obat.setSupplier(med.getSupplier());
        obat.setNamaPBF(med.getNamaPBF());
        obat.setSatuan(med.getSatuan());
        obat.setGolongan(med.getGolongan());
        obat.setIndikasi(med.getIndikasi());
        obat.setHargaBeli(med.getHargaBeli());
        obat.setHargaJualApotek(med.getHargaJualApotek());
        obat.setHargaPlot(med.getHargaPlot());
        obat.setQuantity(med.getQuantity());
        apotekService.tambahStokBarang(obat);
        return ResponseEntity.ok(Map.of("status", "OK", "message", "Obat berhasil ditambahkan"));
    }

    @GetMapping("/medicine")
    public ResponseEntity<?> getAllMedicine() {
        return ResponseEntity.ok(apotekService.getAllObat());
    }

    @PostMapping("/prescription")
    public ResponseEntity<?> processPrescription(@RequestBody PrescriptionRequest req) {
        RiwayatKlinis resep = new RiwayatKlinis();
        resep.setNamaPasien(req.getNamaPasien());
        resep.setNamaObat(req.getNamaObat());
        resep.setDosis(req.getDosis());
        resep.setNamaDokter(req.getNamaDokter());
        resep.setNoPraktek(req.getNoPraktek());
        resep.setNamaRumahSakit(req.getNamaRumahSakit());
        resep.setHargaObat(req.getHargaObat());
        resep.setJumlahObat(req.getJumlahObat() > 0 ? req.getJumlahObat() : 1);
        resep.setTuslah(req.getTuslah());
        resep.setEmbalase(req.getEmbalase());

        List<String> warnings = apotekService.validsaiResep(resep, req.getRiwayatAlergi(), null, null);
        boolean dispensed = apotekService.processDispense(resep);

        return ResponseEntity.ok(Map.of(
                "warnings", warnings,
                "dispensed", dispensed,
                "message", dispensed ? "Obat berhasil diserahkan" : "Gagal - stok tidak cukup"));
    }

    @PostMapping("/stokopname")
    public ResponseEntity<?> stokOpname(@RequestBody Map<String, Object> body) {
        String noFaktur = (String) body.get("noFaktur");
        int fisik = ((Number) body.get("stokFisik")).intValue();
        StokOpname result = apotekService.performStokOpname(noFaktur, fisik);
        if (result == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Obat tidak ditemukan"));
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/stokopname/adjust")
    public ResponseEntity<?> adjustStok(@RequestBody Map<String, Object> body) {
        String noFaktur = (String) body.get("noFaktur");
        int fisik = ((Number) body.get("stokFisik")).intValue();
        apotekService.adjustStok(noFaktur, fisik);
        return ResponseEntity.ok(Map.of("status", "OK", "message", "Stok disesuaikan ke " + fisik));
    }
}
