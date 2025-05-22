package com.Tingeso.backend.Repository;

import com.Tingeso.backend.Entity.ReservaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaRepository extends JpaRepository<ReservaEntity, Long> {
    List<ReservaEntity> findAllByRutusuario(String rutusuario);
}
