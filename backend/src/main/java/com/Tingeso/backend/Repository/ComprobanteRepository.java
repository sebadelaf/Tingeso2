package com.Tingeso.backend.Repository;

import com.Tingeso.backend.Entity.ComprobanteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComprobanteRepository extends JpaRepository<ComprobanteEntity, Long> {
    Optional<ComprobanteEntity> findByIdreserva(long id);
}
