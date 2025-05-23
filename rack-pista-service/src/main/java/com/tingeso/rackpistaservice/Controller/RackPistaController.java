package com.tingeso.rackpistaservice.Controller;

import com.tingeso.rackpistaservice.Entity.ReservaEntity;
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

    @GetMapping("/reservas")
    public List<ReservaEntity> reservas(){
        return rackPistaService.obtenerTodasLasReservasExternas();
    }
    }
