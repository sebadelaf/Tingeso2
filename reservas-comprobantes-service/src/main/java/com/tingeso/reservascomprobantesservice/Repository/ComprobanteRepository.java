package com.tingeso.reservascomprobantesservice.Repository;

import com.tingeso.reservascomprobantesservice.Entity.ComprobanteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface ComprobanteRepository extends JpaRepository<ComprobanteEntity, Long> {
    Optional<ComprobanteEntity> findByIdreserva(long id);
}
