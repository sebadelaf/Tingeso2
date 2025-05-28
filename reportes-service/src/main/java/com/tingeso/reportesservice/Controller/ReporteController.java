package com.tingeso.reportesservice.Controller;

import com.tingeso.reportesservice.DTO.ReporteDTO;
import com.tingeso.reportesservice.Service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reportes")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @PostMapping("/tipo-reserva")
    public ResponseEntity<List<List<Long>>> getReporteTipoReserva(@RequestBody ReporteDTO reporteDTO) {
        try {
            List<List<Long>> reporte = reporteService.reportetiporeserva(reporteDTO.getMesinicio(), reporteDTO.getMesfin());
            return ResponseEntity.ok(reporte);
        } catch (IllegalArgumentException e) {
            System.err.println("Error al generar reporte por tipo de reserva: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/por-grupo")
    public ResponseEntity<List<List<Long>>> getReportePorGrupo(@RequestBody ReporteDTO reporteDTO) {
        try {
            List<List<Long>> reporte = reporteService.reporteporgrupo(reporteDTO.getMesinicio(), reporteDTO.getMesfin());
            return ResponseEntity.ok(reporte);
        } catch (IllegalArgumentException e) {
            System.err.println("Error al generar reporte por grupo: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
}
