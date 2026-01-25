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

    @PrePersist
    protected void onCreate() {
        if (tglKunjungan == null)
            tglKunjungan = LocalDate.now();
    }
}
