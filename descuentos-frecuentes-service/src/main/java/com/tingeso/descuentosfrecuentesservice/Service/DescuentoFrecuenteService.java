package com.tingeso.descuentosfrecuentesservice.Service;

import com.tingeso.descuentosfrecuentesservice.Entity.VisitaEntity;
import com.tingeso.descuentosfrecuentesservice.Repository.VisitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth; // Para trabajar con meses

@Service
public class DescuentoFrecuenteService {

    @Autowired
    private VisitaRepository visitaRepository;

    // Método para registrar una visita (puede ser llamado por M5 o el frontend si es pertinente)
    @Transactional
    public VisitaEntity registrarVisita(String rutCliente, LocalDateTime fechaVisita) {
        VisitaEntity visita = new VisitaEntity(null, rutCliente, fechaVisita);
        return visitaRepository.save(visita);
    }

    // Migración de la lógica de calcularDescuentoEspecial de tu ReservaService original
    public float calcularDescuentoEspecial(String rutCliente, float precioInicial) {
        LocalDateTime ahora = LocalDateTime.now();
        YearMonth mesActual = YearMonth.from(ahora);

        // Definir inicio y fin del mes actual
        LocalDateTime inicioMes = mesActual.atDay(1).atStartOfDay();
        LocalDateTime finMes = mesActual.atEndOfMonth().atTime(23, 59, 59);

        Long reservasEnMes = visitaRepository.countVisitasByRutClienteAndFechaBetween(rutCliente, inicioMes, finMes);

        // Reglas de descuento [cite: 25]
        if (reservasEnMes <= 1) { // 0 a 1 vez [cite: 25]
            return 0f; // 0% [cite: 25]
        } else if (reservasEnMes <= 4) { // 2 a 4 veces [cite: 25]
            return 0.1f * precioInicial; // 10% descuento [cite: 25]
        } else if (reservasEnMes <= 6) { // 5 a 6 veces [cite: 25]
            return 0.2f * precioInicial; // 20% descuento [cite: 25]
        } else { // 7 a MAS veces [cite: 25]
            return 0.3f * precioInicial; // 30% descuento [cite: 25]
        }
    }
}
