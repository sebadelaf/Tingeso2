package com.tingeso.descuentosgruposervice.Repository;

import com.tingeso.descuentosgruposervice.Entity.DescuentoGrupoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface DescuentoGrupoRepository extends JpaRepository<DescuentoGrupoEntity, Long> {
    // Un método para encontrar el descuento aplicable por rango
    DescuentoGrupoEntity findByMinPersonasLessThanEqualAndMaxPersonasGreaterThanEqual(int cantidadPersonas1, int cantidadPersonas2);
}