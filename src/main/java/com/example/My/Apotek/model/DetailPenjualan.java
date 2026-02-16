package com.example.My.Apotek.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "detail_penjualan")
@Data
public class DetailPenjualan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "penjualan_id")
    private Penjualan penjualan;

    @Column(name = "nama_obat")
    private String namaObat;

    @Column(name = "harga_jual")
    private Double hargaJual;

    private Integer jumlah;

    private Double subtotal;

    @PrePersist
    @PreUpdate
    protected void calculateSubtotal() {
        if (hargaJual != null && jumlah != null) {
            subtotal = hargaJual * jumlah;
        }
    }
}
