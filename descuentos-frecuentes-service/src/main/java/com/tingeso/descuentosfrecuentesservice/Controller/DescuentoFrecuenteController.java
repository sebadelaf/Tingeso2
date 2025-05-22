package com.tingeso.descuentosfrecuentesservice.Controller;

import com.tingeso.descuentosfrecuentesservice.Entity.VisitaEntity;
import com.tingeso.descuentosfrecuentesservice.Service.DescuentoFrecuenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/descuentos-frecuentes")
@CrossOrigin("*")
public class DescuentoFrecuenteController {

    @Autowired
    private DescuentoFrecuenteService descuentoFrecuenteService;

    // Endpoint para registrar una visita (útil para pruebas y poblar datos)
    @PostMapping("/visita")
    public ResponseEntity<VisitaEntity> registerVisita(
            @RequestParam String rutCliente,
            @RequestParam(required = false) String fechaVisita) { // Opcional: si quieres especificar la fecha
        LocalDateTime dateToUse = (fechaVisita != null && !fechaVisita.isEmpty()) ?
                LocalDateTime.parse(fechaVisita) : LocalDateTime.now();
        VisitaEntity visita = descuentoFrecuenteService.registrarVisita(rutCliente, dateToUse);
        return ResponseEntity.ok(visita);
    }

    // Endpoint para calcular el descuento especial
    @GetMapping("/calcular")
    public ResponseEntity<Float> getDescuentoEspecial(
            @RequestParam String rutCliente,
            @RequestParam float precioInicial) {
        float descuento = descuentoFrecuenteService.calcularDescuentoEspecial(rutCliente, precioInicial);
        return ResponseEntity.ok(descuento);
    }
}
