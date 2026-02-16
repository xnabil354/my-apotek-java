package com.example.My.Apotek.repository;

import com.example.My.Apotek.model.RiwayatKlinis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RiwayatKlinisRepository extends JpaRepository<RiwayatKlinis, Long> {
    List<RiwayatKlinis> findByTglKunjunganBetween(LocalDate start, LocalDate end);

    List<RiwayatKlinis> findByTglKunjungan(LocalDate date);
}
