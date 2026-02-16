package com.example.My.Apotek.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "penjualan")
@Data
public class Penjualan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "no_transaksi", unique = true)
    private String noTransaksi;

    private LocalDateTime tanggal;

    @Column(name = "nama_pembeli")
    private String namaPembeli;

    @Column(name = "total_harga")
    private Double totalHarga;

    @OneToMany(mappedBy = "penjualan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetailPenjualan> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (tanggal == null)
            tanggal = LocalDateTime.now();
        if (noTransaksi == null) {
            noTransaksi = "TRX-" + System.currentTimeMillis();
        }
    }
}
