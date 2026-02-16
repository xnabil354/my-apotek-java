package com.example.My.Apotek.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "riwayat_klinis")
@Data
public class RiwayatKlinis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nik;

    @Column(name = "nama_pasien")
    private String namaPasien;

    private String diagnosa;

    @Column(name = "nama_obat")
    private String namaObat;

    private String dosis;

    @Column(name = "tgl_kunjungan")
    private LocalDate tglKunjungan;

    @Column(name = "nama_dokter")
    private String namaDokter;

    @Column(name = "no_praktek")
    private String noPraktek;

    @Column(name = "nama_rumah_sakit")
    private String namaRumahSakit;

    @Column(name = "harga_obat")
    private Double hargaObat;

    @Column(name = "jumlah_obat")
    private Integer jumlahObat;

    private Double tuslah;

    private Double embalase;

    @Column(name = "total_harga")
    private Double totalHarga;

    @PrePersist
    protected void onCreate() {
        if (tglKunjungan == null)
            tglKunjungan = LocalDate.now();
        calculateTotal();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateTotal();
    }

    private void calculateTotal() {
        double harga = (hargaObat != null ? hargaObat : 0.0);
        int qty = (jumlahObat != null ? jumlahObat : 1);
        double t = (tuslah != null ? tuslah : 0.0);
        double e = (embalase != null ? embalase : 0.0);
        totalHarga = (harga * qty) + t + e;
    }
}
