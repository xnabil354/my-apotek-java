package com.example.My.Apotek.repository;

import com.example.My.Apotek.model.Penjualan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PenjualanRepository extends JpaRepository<Penjualan, Long> {
    List<Penjualan> findByTanggalBetween(LocalDateTime start, LocalDateTime end);
}
