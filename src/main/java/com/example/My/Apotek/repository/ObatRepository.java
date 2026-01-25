package com.example.My.Apotek.repository;

import com.example.My.Apotek.model.Obat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ObatRepository extends JpaRepository<Obat, Long> {
    Optional<Obat> findByBarcode(String barcode);

    Optional<Obat> findByNamaObat(String namaObat);
}
