package com.tingeso.tarifasservice.Controller;

import com.tingeso.tarifasservice.Entity.TarifaEntity;
import com.tingeso.tarifasservice.Service.TarifaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tarifas")
public class TarifaController {

    @Autowired
    private TarifaService tarifaService;

    @GetMapping("/todas")
    public ResponseEntity<List<TarifaEntity>> getAllTarifas() {
        List<TarifaEntity> tarifas = tarifaService.obtenerTodasLasTarifas();
        return ResponseEntity.ok(tarifas);
    }

    @GetMapping("/tipo/{tipoReserva}")
    public ResponseEntity<TarifaEntity> getTarifaByTipoReserva(@PathVariable int tipoReserva) {
        TarifaEntity tarifa = tarifaService.obtenerTarifaPorTipoReserva(tipoReserva);
        if (tarifa != null) {
            return ResponseEntity.ok(tarifa);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/precio-inicial")
    public ResponseEntity<Float> getPrecioInicial(
            @RequestParam int tipoReserva,
            @RequestParam int cantidadPersonas) {
        try {
            float precioInicial = tarifaService.calcularPrecioInicialPorTipo(tipoReserva, cantidadPersonas);
            return ResponseEntity.ok(precioInicial);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null); // O un DTO de error más específico
        }
    }

}
