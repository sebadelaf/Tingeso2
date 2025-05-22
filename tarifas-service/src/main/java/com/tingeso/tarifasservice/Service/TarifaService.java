package com.tingeso.tarifasservice.Service;

import com.tingeso.tarifasservice.Entity.TarifaEntity;
import com.tingeso.tarifasservice.Repository.TarifaRepository;
import jakarta.annotation.PostConstruct; // Import para PostConstruct
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TarifaService {

    @Autowired
    private TarifaRepository tarifaRepository;

    // Método para inicializar datos si la tabla está vacía
    @PostConstruct
    public void initData() {
        if (tarifaRepository.count() == 0) {
            tarifaRepository.save(new TarifaEntity(null, 1, 15000.0f, 30, "10 vueltas o máx 10 min")); // [cite: 17]
            tarifaRepository.save(new TarifaEntity(null, 2, 20000.0f, 35, "15 vueltas o máx 15 min")); // [cite: 17]
            tarifaRepository.save(new TarifaEntity(null, 3, 25000.0f, 40, "20 vueltas o máx 20 min")); // [cite: 17]
            System.out.println("Datos iniciales de tarifas cargados.");
        }
    }
    public TarifaEntity obtenerTarifaPorTipoReserva(int tipoReserva) {
        return tarifaRepository.findByTipoReserva(tipoReserva);
    }

    public List<TarifaEntity> obtenerTodasLasTarifas() {
        return tarifaRepository.findAll();
    }

    // Puedes añadir un método para calcular el precio inicial aquí
    public float calcularPrecioInicialPorTipo(int tipoReserva, int cantidadPersonas) {
        TarifaEntity tarifa = tarifaRepository.findByTipoReserva(tipoReserva);
        if (tarifa == null || cantidadPersonas <= 0 || tipoReserva>3) {
            throw new IllegalArgumentException("Tipo de reserva inválido: " + tipoReserva);
        }
        return tarifa.getPrecioBase() * cantidadPersonas;
    }
}
