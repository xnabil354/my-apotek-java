package com.example.My.Apotek.desktop;

import lombok.Data;

@Data
public class PrescriptionRequest {
    private String namaPasien;
    private int usia;
    private String riwayatAlergi;
    private String namaObat;
    private String dosis;
    private double beratBadan;
    private String namaDokter;
    private String noPraktek;
    private String namaRumahSakit;
    private double hargaObat;
    private int jumlahObat;
    private double tuslah;
    private double embalase;
}