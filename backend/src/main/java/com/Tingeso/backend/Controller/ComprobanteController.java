package com.Tingeso.backend.Controller;

import com.Tingeso.backend.DTO.ReporteDTO;
import com.Tingeso.backend.Entity.ComprobanteEntity;
import com.Tingeso.backend.Entity.ReservaEntity;
import com.Tingeso.backend.Repository.ComprobanteRepository;
import com.Tingeso.backend.Service.ComprobanteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/comprobantes")
public class ComprobanteController {
    @Autowired
    public ComprobanteService comprobanteService;


    @PostMapping("/crear/{idreserva}")
    public ComprobanteEntity crearcomprobante(@PathVariable long idreserva) {
        return comprobanteService.crearcomprobante(idreserva);
    }

    @PostMapping("/reportetipo")
    public List<Object> reportetiporeserva(@RequestBody ReporteDTO reporteDTO) {
        String mesInicio = reporteDTO.getMesinicio();
        String mesFin = reporteDTO.getMesfin();
        return comprobanteService.reporteportiporeserva(mesInicio, mesFin);
    }

    @PostMapping("/reportegrupos")
    public List<Object> reporteporgrupos(@RequestBody ReporteDTO reporteDTO) {
        String mesInicio = reporteDTO.getMesinicio();
        String mesFin = reporteDTO.getMesfin();
        return comprobanteService.reporteporgrupo(mesInicio, mesFin);
    }



}
