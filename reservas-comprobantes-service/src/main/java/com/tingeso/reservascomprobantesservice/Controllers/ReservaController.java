package com.tingeso.reservascomprobantesservice.Controllers;

import com.tingeso.reservascomprobantesservice.Entity.ReservaEntity;
import com.tingeso.reservascomprobantesservice.Repository.ReservaRepository;
import com.tingeso.reservascomprobantesservice.Service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/reservas")
public class ReservaController {
    @Autowired
    public ReservaService reservaService;
    @Autowired
    public ReservaRepository reservaRepository;

    // Controller CrearReserva
    @PostMapping("/crear")
    public ReservaEntity crearReserva(@RequestBody ReservaEntity reserva) {
        String fechahora = reserva.getFechahora();
        int tiporeserva = reserva.getTiporeserva();
        int cantidadpersonas = reserva.getCantidadpersonas();
        String rutuser = reserva.getRutusuario();
        String nombreuser = reserva.getNombreusuario();
        String email = reserva.getEmail();
        int cantidadcumple = reserva.getCantidadcumple();
        return reservaService.crearReserva(fechahora,tiporeserva,cantidadpersonas,cantidadcumple,nombreuser,rutuser,email);
    }
/*
*     // Controller ObtenerReserva
    @GetMapping("/obtenerReservas/{id}")
    public List<ReservaEntity> obtenerReservas(@PathVariable String rutuser) {
        return reservaService.obtenerReservasUsuario(rutuser);
    }
    // Controller calcularprecioinicial
    @GetMapping("/calcularprecioinicial/{id}")
    public float calcularPrecioInicial(@PathVariable Long idreserva) {
        return reservaService.calcularprecioinicial(idreserva);
    }
* */

    @GetMapping("/todas")
    public List<ReservaEntity> obtenerreservas(){
        return reservaRepository.findAll();
    }
}