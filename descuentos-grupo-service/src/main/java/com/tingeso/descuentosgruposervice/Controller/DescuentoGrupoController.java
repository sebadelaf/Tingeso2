package com.tingeso.descuentosgruposervice.Controller;

import com.tingeso.descuentosgruposervice.Service.DescuentoGrupoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/descuentos-grupo")
public class DescuentoGrupoController {

    @Autowired
    private DescuentoGrupoService descuentoGrupoService;

    // Endpoint para calcular el descuento por grupo
    @GetMapping("/calcular")
    public ResponseEntity<Float> getDescuentoGrupo(
            @RequestParam int cantidadPersonas,
            @RequestParam float precioInicial) {
        float descuento = descuentoGrupoService.calcularDescuentoGrupo(cantidadPersonas, precioInicial);
        return ResponseEntity.ok(descuento);
    }
}
