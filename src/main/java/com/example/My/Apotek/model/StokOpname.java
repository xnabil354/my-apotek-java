package com.example.My.Apotek.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "stok_opname")
@Data
public class StokOpname {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nama_obat")
    private String namaObat;

    @Column(name = "no_faktur")
    private String noFaktur;

    @Column(name = "stok_sistem")
    private Integer stokSistem;

    @Column(name = "stok_fisik")
    private Integer stokFisik;

    private Integer selisih;

    private String status;

    private LocalDateTime tanggal;

    @PrePersist
    protected void onCreate() {
        if (tanggal == null)
            tanggal = LocalDateTime.now();
        if (stokSistem != null && stokFisik != null) {
            if (selisih == null)
                selisih = stokFisik - stokSistem;
            if (status == null) {
                double persen = stokSistem > 0 ? (Math.abs(selisih) * 100.0 / stokSistem)
                        : (selisih == 0 ? 0 : 100);
                status = (persen > 5.0 && Math.abs(selisih) > 1) ? "SELISIH TINGGI" : "NORMAL";
            }
        }
    }
}
