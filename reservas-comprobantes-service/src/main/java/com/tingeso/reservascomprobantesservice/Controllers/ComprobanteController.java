package com.tingeso.reservascomprobantesservice.Controllers;


import com.tingeso.reservascomprobantesservice.DTO.ReporteDTO;
import com.tingeso.reservascomprobantesservice.Entity.ComprobanteEntity;
import com.tingeso.reservascomprobantesservice.Service.ComprobanteService;
import org.springframework.beans.factory.annotation.Autowired;
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