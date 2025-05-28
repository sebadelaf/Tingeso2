package com.tingeso.rackpistaservice.Controller;

import com.tingeso.rackpistaservice.Entity.ReservaEntity;
import com.tingeso.rackpistaservice.Repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/rack")
public class RackPistaController {

    @Autowired
    private ReservaRepository reservaRepository;

    @GetMapping("/reservas")
    public List<ReservaEntity> reservas(){
        return reservaRepository.findAll();
    }
    }
