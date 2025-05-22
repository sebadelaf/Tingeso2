package com.tingeso.reservascomprobantesservice.Repository;

import com.tingeso.reservascomprobantesservice.Entity.ReservaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ReservaRepository extends JpaRepository<ReservaEntity, Long> {
    List<ReservaEntity> findAllByRutusuario(String rutusuario);
}