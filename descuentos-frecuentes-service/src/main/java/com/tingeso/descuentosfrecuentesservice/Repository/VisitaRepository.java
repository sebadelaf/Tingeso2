package com.tingeso.descuentosfrecuentesservice.Repository;

import com.tingeso.descuentosfrecuentesservice.Entity.VisitaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitaRepository extends JpaRepository<VisitaEntity, Long> {
    List<VisitaEntity> findByRutCliente(String rutCliente);

    // Método para contar visitas en un rango de fechas para un RUT específico
    @Query("SELECT COUNT(v) FROM VisitaEntity v WHERE v.rutCliente = :rutCliente AND v.fechaVisita BETWEEN :startDate AND :endDate")
    Long countVisitasByRutClienteAndFechaBetween(String rutCliente, LocalDateTime startDate, LocalDateTime endDate);
}
