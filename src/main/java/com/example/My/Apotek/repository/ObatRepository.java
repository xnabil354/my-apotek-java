package com.example.My.Apotek.repository;

import com.example.My.Apotek.model.Obat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ObatRepository extends JpaRepository<Obat, Long> {
    Optional<Obat> findByNoFaktur(String noFaktur);

    List<Obat> findByNamaObat(String namaObat);

    List<Obat> findByNamaObatContainingIgnoreCase(String keyword);

    List<Obat> findByGolonganIgnoreCase(String golongan);

    List<Obat> findByNamaPBFIgnoreCase(String namaPBF);
}
