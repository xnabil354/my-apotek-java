package com.example.My.Apotek.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "riwayat_barang")
@Data
public class RiwayatBarang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nama_obat")
    private String namaObat;

    @Column(name = "nomor_batch")
    private String nomorBatch;

    private String tipe; // MASUK / KELUAR

    private Integer quantity;

    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
