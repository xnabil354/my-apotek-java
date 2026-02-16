package com.example.My.Apotek.desktop;

import lombok.Data;

@Data
public class Medicine {
    private String namaObat;
    private String nomorBatch;
    private String tglProduksi;
    private String tglExpired;
    private String supplier;
    private String noFaktur;
    private String namaPBF;
    private String satuan;
    private String golongan;
    private String indikasi;
    private double hargaBeli;
    private double hargaJualApotek;
    private double hargaPlot;
    private double hargaBeliPpn;
    private int quantity;
}
