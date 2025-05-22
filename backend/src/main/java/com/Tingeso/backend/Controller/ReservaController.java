package com.Tingeso.backend.Controller;

import com.Tingeso.backend.DTO.DescuentoCumpleDTO;
import com.Tingeso.backend.DTO.DescuentoEspecialDTO;
import com.Tingeso.backend.DTO.DescuentoGrupoDTO;
import com.Tingeso.backend.Entity.ReservaEntity;
import com.Tingeso.backend.Repository.ReservaRepository;
import com.Tingeso.backend.Service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
    // Controller ObtenerReserva
    @GetMapping("/obtenerReservas/{id}")
    public List<ReservaEntity> obtenerReservas(@PathVariable String rutuser) {
        return reservaService.obtenerReservasUsuario(rutuser);
    }
    // Controller calcularprecioinicial
    @GetMapping("/calcularprecioinicial/{id}")
    public float calcularPrecioInicial(@PathVariable Long idreserva) {
        return reservaService.calcularprecioinicial(idreserva);
    }

    @GetMapping("/todas")
    public List<ReservaEntity> obtenerreservas(){
        return reservaRepository.findAll();
    }
}
