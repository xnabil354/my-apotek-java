package com.example.My.Apotek.desktop;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Medicine {

    private String sku; // Scan/SKU [cite: 5]
    private String namaObat; // Nama Obat/Barang [cite: 6]
    private String nomorBatch; // Nomor Batch [cite: 7]
    private LocalDate tglProduksi; // Tanggal Produksi [cite: 8]
    private LocalDate tglExpired; // Tanggal Expired [cite: 9]
    private String supplier; // Supplier [cite: 10]
    private double hargaBeli; // Harga Beli [cite: 11]
    private int quantity; // Quantity [cite: 12]
}
