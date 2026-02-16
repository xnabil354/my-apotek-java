package com.example.My.Apotek.repository;

import com.example.My.Apotek.model.RiwayatBarang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RiwayatBarangRepository extends JpaRepository<RiwayatBarang, Long> {
    List<RiwayatBarang> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<RiwayatBarang> findByNamaPBFIgnoreCase(String namaPBF);

    List<RiwayatBarang> findByTipe(String tipe);
}
