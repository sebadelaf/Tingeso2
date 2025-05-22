package com.tingeso.tarifasservice.Repository;


import com.tingeso.tarifasservice.Entity.TarifaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface TarifaRepository extends JpaRepository<TarifaEntity, Long> {
    // Puedes añadir métodos personalizados si los necesitas, ej:
    TarifaEntity findByTipoReserva(int tipoReserva);
}
