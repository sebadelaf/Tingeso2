package com.tingeso.rackpistaservice.Controller;

import com.tingeso.rackpistaservice.Entity.BloqueHorarioEntity;
import com.tingeso.rackpistaservice.Service.RackPistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/rack")
@CrossOrigin("*")
public class RackPistaController {

    @Autowired
    private RackPistaService rackPistaService;

    // Endpoint para registrar un bloque horario basado en una reserva de M5
    // Se llamará desde M5 cuando se cree una reserva exitosamente
    @PostMapping("/registrar-desde-reserva/{idReserva}")
    public ResponseEntity<BloqueHorarioEntity> registrarBloque(@PathVariable Long idReserva) {
        try {
            BloqueHorarioEntity bloque = rackPistaService.registrarBloqueDesdeReserva(idReserva);
            return ResponseEntity.ok(bloque);
        } catch (IllegalArgumentException e) {
            System.err.println("Error al registrar bloque en Rack Pista: " + e.getMessage());
            return ResponseEntity.badRequest().body(null); // Podrías devolver un DTO de error
        }
    }

    // Endpoint para cancelar un bloque horario (ej. si una reserva se cancela)
    @PutMapping("/cancelar/{idReserva}")
    public ResponseEntity<BloqueHorarioEntity> cancelarBloque(@PathVariable Long idReserva) {
        try {
            BloqueHorarioEntity bloque = rackPistaService.cancelarBloque(idReserva);
            return ResponseEntity.ok(bloque);
        } catch (IllegalArgumentException e) {
            System.err.println("Error al cancelar bloque en Rack Pista: " + e.getMessage());
            return ResponseEntity.notFound().build(); // O un DTO de error
        }
    }

    // Endpoint para obtener el rack semanal
    @GetMapping("/semanal/{fechaInicio}")
    public ResponseEntity<List<BloqueHorarioEntity>> getRackSemanal(@PathVariable String fechaInicio) {
        try {
            List<BloqueHorarioEntity> rack = rackPistaService.obtenerRackSemanal(fechaInicio);
            return ResponseEntity.ok(rack);
        } catch (IllegalArgumentException e) {
            System.err.println("Error al obtener rack semanal: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/todos")
    public ResponseEntity<List<BloqueHorarioEntity>> getAllBloques() {
        return ResponseEntity.ok(rackPistaService.obtenerTodosLosBloques());
    }
}
