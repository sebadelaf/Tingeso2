package com.tingeso.rackpistaservice.Repository;

import com.tingeso.rackpistaservice.Entity.BloqueHorarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BloqueHorarioRepository extends JpaRepository<BloqueHorarioEntity, Long> {
    // Para verificar superposición y obtener rack
    List<BloqueHorarioEntity> findByInicioBloqueBetweenOrFinBloqueBetweenOrInicioBloqueLessThanEqualAndFinBloqueGreaterThanEqual(
            LocalDateTime start1, LocalDateTime end1,
            LocalDateTime start2, LocalDateTime end2,
            LocalDateTime start3, LocalDateTime end3
    );

    // Para obtener un bloque por ID de reserva
    BloqueHorarioEntity findByIdReserva(Long idReserva);

    // Para obtener el rack por un rango de fechas (ej: una semana)
    List<BloqueHorarioEntity> findByInicioBloqueBetweenOrderByInicioBloqueAsc(LocalDateTime start, LocalDateTime end);
}
