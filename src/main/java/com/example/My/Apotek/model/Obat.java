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

    @Column(name = "no_faktur")
    private String noFaktur;

    private String satuan;

    @Column(name = "nama_pbf")
    private String namaPBF;

    @Column(name = "harga_beli")
    private Double hargaBeli;

    @Column(name = "harga_jual_apotek")
    private Double hargaJualApotek;

    @Column(name = "harga_plot")
    private Double hargaPlot;

    @Column(name = "harga_beli_ppn")
    private Double hargaBeliPpn;

    private Integer quantity;

    private String golongan;

    @Column(name = "diskon_persen")
    private Double diskonPersen;

    @Column(name = "metode_pembayaran")
    private String metodePembayaran;

    @Column(name = "jatuh_tempo")
    private Integer jatuhTempo;

    @Column(name = "jenis_faktur")
    private String jenisFaktur;

    @Column(name = "indikasi", length = 1000)
    private String indikasi;

    @PrePersist
    @PreUpdate
    protected void calculatePpn() {
        if (hargaBeli != null) {
            hargaBeliPpn = hargaBeli * 1.11;
        }
    }
}
