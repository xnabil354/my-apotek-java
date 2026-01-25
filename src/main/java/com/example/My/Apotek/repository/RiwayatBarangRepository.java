package com.example.My.Apotek.repository;

import com.example.My.Apotek.model.RiwayatBarang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RiwayatBarangRepository extends JpaRepository<RiwayatBarang, Long> {
}
