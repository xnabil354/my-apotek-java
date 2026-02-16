package com.example.My.Apotek.repository;

import com.example.My.Apotek.model.StokOpname;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StokOpnameRepository extends JpaRepository<StokOpname, Long> {
    List<StokOpname> findAllByOrderByTanggalDesc();
}
