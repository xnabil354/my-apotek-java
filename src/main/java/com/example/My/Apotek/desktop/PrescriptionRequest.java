package com.example.My.Apotek.desktop;

import lombok.Data;

@Data
public class PrescriptionRequest {
    private String namaPasien;
    private int usia;
    private String riwayatAlergi;
    private String namaObat;
    private double dosis;
    private double beratBadan;
}