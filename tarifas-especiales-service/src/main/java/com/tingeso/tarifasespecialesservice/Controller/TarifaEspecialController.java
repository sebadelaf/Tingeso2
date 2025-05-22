package com.tingeso.tarifasespecialesservice.Controller;

import com.tingeso.tarifasespecialesservice.Service.TarifaEspecialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tarifas-especiales")
@CrossOrigin("*")
public class TarifaEspecialController {

    @Autowired
    private TarifaEspecialService tarifaEspecialService;

    // Endpoint para aplicar descuentos por días especiales (fines de semana/feriados)
    @GetMapping("/aplicar-descuento-dia-especial")
    public ResponseEntity<Float> applySpecialDayDiscount(
            @RequestParam String fechahora,
            @RequestParam float precioInicialBase) {
        try {
            float precioFinal = tarifaEspecialService.aplicarDescuentoPorDiaEspecial(fechahora, precioInicialBase);
            return ResponseEntity.ok(precioFinal);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null); // O un DTO de error más específico
        }
    }

    // Endpoint para calcular el descuento por cumpleaños
    @GetMapping("/calcular-descuento-cumpleanos")
    public ResponseEntity<Float> getCumpleanosDiscount(
            @RequestParam int cantidadPersonas,
            @RequestParam float precioInicialOriginal,
            @RequestParam int cantidadCumple) {
        float descuento = tarifaEspecialService.calcularDescuentoCumpleanos(cantidadPersonas, precioInicialOriginal, cantidadCumple);
        return ResponseEntity.ok(descuento);
    }
}