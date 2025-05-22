package com.tingeso.descuentosgruposervice.Service;

import com.tingeso.descuentosgruposervice.Entity.DescuentoGrupoEntity;
import com.tingeso.descuentosgruposervice.Repository.DescuentoGrupoRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DescuentoGrupoService {

    @Autowired
    private DescuentoGrupoRepository descuentoGrupoRepository;

    @PostConstruct
    public void initData() {
        if (descuentoGrupoRepository.count() == 0) {
            // Carga los descuentos según la tabla de la evaluación
            // "1-2 personas", "0%" [cite: 21]
            // "3-5 personas", "10%" [cite: 21]
            // "6-10 personas", "20%" [cite: 21]
            // "11-15 personas", "30%" [cite: 21]
            descuentoGrupoRepository.save(new DescuentoGrupoEntity(null, 1, 2, 0.00f, "1-2 personas"));
            descuentoGrupoRepository.save(new DescuentoGrupoEntity(null, 3, 5, 0.10f, "3-5 personas"));
            descuentoGrupoRepository.save(new DescuentoGrupoEntity(null, 6, 10, 0.20f, "6-10 personas"));
            descuentoGrupoRepository.save(new DescuentoGrupoEntity(null, 11, 15, 0.30f, "11-15 personas"));
            System.out.println("Datos iniciales de descuentos por grupo cargados.");
        }
    }

    // Migración de la lógica de calcularDescuentoGrupo de tu ReservaService original
    public float calcularDescuentoGrupo(int cantidadPersonas, float precioInicial) {
        DescuentoGrupoEntity descuento = descuentoGrupoRepository.findByMinPersonasLessThanEqualAndMaxPersonasGreaterThanEqual(cantidadPersonas, cantidadPersonas);
        if (descuento != null) {
            return descuento.getPorcentajeDescuento() * precioInicial;
        }
        return 0f; // Si no se encuentra un rango, o si la cantidad de personas excede los rangos definidos.
    }
}
