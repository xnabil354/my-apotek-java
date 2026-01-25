package com.example.My.Apotek.desktop;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.My.Apotek.model.Obat;
import com.example.My.Apotek.repository.ObatRepository;
import java.util.List;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/apotek")
@CrossOrigin(origins = "*") // Allow frontend access
public class ApotekController {

    @Autowired
    private ObatRepository obatRepository;

    // --- ALUR 1: BARANG DATANG (CRUD) ---
    @PostMapping("/obat")
    public Obat tambahObat(@RequestBody Obat obat) {
        return obatRepository.save(obat);
    }

    @GetMapping("/obat")
    public List<Obat> getAllObat() {
        return obatRepository.findAll();
    }

    // Alur 1: Rule Pengingat Kadaluarsa [cite: 15]
    @GetMapping("/cek-kadaluarsa")
    public String cekKadaluarsa(@RequestParam String tglExp) {
        LocalDate exp = LocalDate.parse(tglExp);
        long selisihBulan = ChronoUnit.MONTHS.between(LocalDate.now(), exp);

        if (selisihBulan <= 3) {
            return "ALERT: Stok hampir kadaluarsa (Kurang dari 3 bulan)!";
        }
        return "Stok dalam kondisi aman.";
    }

    // Alur 2: Rule Engine Resep (Klinis)
    @PostMapping("/proses-resep")
    public String prosesResep(@RequestBody PrescriptionRequest req) {
        // Rule: Cek Alergi [cite: 20]
        if (req.getRiwayatAlergi().equalsIgnoreCase(req.getNamaObat())) {
            return "❌ BERBAHAYA: Kontraindikasi! Pasien alergi terhadap obat ini.";
        }

        // Rule: Cek Dosis Anak (Usia < 12)
        if (req.getUsia() < 12 && req.getDosis() > 500) {
            return "⚠️ PERINGATAN: Dosis terlalu tinggi untuk pasien anak.";
        }

        // Rule: Cek Fungsi Ginjal (Simulasi)
        if (req.getUsia() > 65) {
            return "⚠️ PERINGATAN: Pasien Lansia, harap cek fungsi ginjal sebelum dispensing.";
        }

        return "✅ AMAN: Resep tervalidasi oleh Rule Engine.";
    }

    // Alur 3: Rule Stok Opname
    @GetMapping("/stok-opname")
    public String hitungStok(@RequestParam double fisik, @RequestParam double sistem) {
        double selisihPersen = Math.abs((fisik - sistem) / sistem) * 100;

        if (selisihPersen > 5) {
            return "STATUS: Butuh Approval (Selisih " + selisihPersen + "% > toleransi 5%)";
        }
        return "STATUS: Auto Adjust (Selisih " + selisihPersen + "% masuk toleransi)";
    }
}
