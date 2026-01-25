package com.example.My.Apotek.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "obat")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Obat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nama_obat")
    private String namaObat;

    @Column(name = "nomor_batch")
    private String nomorBatch;

    @Column(name = "tgl_produksi")
    private LocalDate tglProduksi;

    @Column(name = "tgl_expired")
    private LocalDate tglExpired;

    private String supplier;

    @Column(name = "harga_beli")
    private Double hargaBeli;

    private Integer quantity;

    private String barcode;
}
