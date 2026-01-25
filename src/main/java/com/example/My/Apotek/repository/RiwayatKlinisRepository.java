package com.example.My.Apotek.repository;

import com.example.My.Apotek.model.RiwayatKlinis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RiwayatKlinisRepository extends JpaRepository<RiwayatKlinis, Long> {
}
