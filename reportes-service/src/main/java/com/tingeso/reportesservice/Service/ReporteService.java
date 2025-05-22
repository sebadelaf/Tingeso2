package com.tingeso.reportesservice.Service;

import com.tingeso.reportesservice.DTO.ComprobanteDTO;
import com.tingeso.reportesservice.DTO.ReservaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    @Autowired
    private RestTemplate restTemplate; // Para llamar a M5 (reservas-comprobantes-service)

    // URL base del servicio de reservas y comprobantes (M5)
    private final String COMPROBANTES_SERVICE_URL = "http://localhost:8080/comprobantes";
    private final String RESERVAS_SERVICE_URL = "http://localhost:8080/reservas";


    // Adaptación de reporteporgrupo de tu ComprobanteService original
    public List<List<Long>> reporteporgrupo(String mesinicio, String mesfin) {
        // Validación de meses como en tu ComprobanteService original
        if (mesinicio == null || mesfin == null) {
            throw new IllegalArgumentException("Los meses de inicio y fin no pueden ser nulos");
        }
        DateTimeFormatter formatoMes = DateTimeFormatter.ofPattern("yyyy-MM");
        YearMonth inicio;
        YearMonth fin;
        try {
            inicio = YearMonth.parse(mesinicio, formatoMes);
            fin = YearMonth.parse(mesfin, formatoMes);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha inválido, debe ser YYYY-MM");
        }
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("El mes de inicio debe ser anterior o igual al mes de fin");
        }

        // Obtener todas las reservas y comprobantes del servicio M5
        // Esto es una simplificación; en un caso real, M5 debería tener un endpoint
        // que filtre por rango de fechas para evitar transferir todos los datos
        ResponseEntity<List<ReservaDTO>> reservasResponse = restTemplate.exchange(
                RESERVAS_SERVICE_URL + "/todas",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ReservaDTO>>() {}
        );
        List<ReservaDTO> reservas = reservasResponse.getBody();
        if (reservas == null) reservas = new ArrayList<>();

        ResponseEntity<List<ComprobanteDTO>> comprobantesResponse = restTemplate.exchange(
                COMPROBANTES_SERVICE_URL + "/todas", // Suponiendo que hay un endpoint /todas en M5 para comprobantes
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ComprobanteDTO>>() {}
        );
        List<ComprobanteDTO> comprobantes = comprobantesResponse.getBody();
        if (comprobantes == null) comprobantes = new ArrayList<>();

        // Crear un mapa para buscar comprobantes por idReserva
        Map<Long, ComprobanteDTO> comprobantesMap = comprobantes.stream()
                .collect(Collectors.toMap(ComprobanteDTO::getIdreserva, c -> c));


        int mesesRango = (fin.getYear() - inicio.getYear()) * 12 + fin.getMonthValue() - inicio.getMonthValue() + 1;
        List<List<Long>> reporte = new ArrayList<>(mesesRango);

        YearMonth mesActual = inicio;
        for (int i = 0; i < mesesRango; i++) {
            LocalDateTime inicioMes = mesActual.atDay(1).atStartOfDay();
            LocalDateTime finMes = mesActual.atEndOfMonth().atTime(23, 59, 59);

            List<ReservaDTO> reservasMes = reservas.stream()
                    .filter(reserva -> {
                        LocalDateTime fechaReserva = LocalDateTime.parse(reserva.getFechahora(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                        return !fechaReserva.isBefore(inicioMes) && !fechaReserva.isAfter(finMes);
                    })
                    .collect(Collectors.toList());

            long totalGrupo1_2 = 0L;
            long totalGrupo3_5 = 0L;
            long totalGrupo6_10 = 0L;
            long totalGrupo11_15 = 0L;

            for (ReservaDTO r : reservasMes) {
                ComprobanteDTO comprobante = comprobantesMap.get(r.getId());
                if (comprobante != null) {
                    long precioFinal = (long) comprobante.getPreciofinal();
                    if (r.getCantidadpersonas() >= 1 && r.getCantidadpersonas() <= 2) {
                        totalGrupo1_2 += precioFinal;
                    } else if (r.getCantidadpersonas() >= 3 && r.getCantidadpersonas() <= 5) {
                        totalGrupo3_5 += precioFinal;
                    } else if (r.getCantidadpersonas() >= 6 && r.getCantidadpersonas() <= 10) {
                        totalGrupo6_10 += precioFinal;
                    } else if (r.getCantidadpersonas() >= 11 && r.getCantidadpersonas() <= 15) {
                        totalGrupo11_15 += precioFinal;
                    }
                }
            }
            reporte.add(List.of(totalGrupo1_2, totalGrupo3_5, totalGrupo6_10, totalGrupo11_15));
            mesActual = mesActual.plusMonths(1);
        }
        return reporte;
    }

    // Adaptación de reportetiporeserva de tu ComprobanteService original
    public List<List<Long>> reportetiporeserva(String mesinicio, String mesfin) {
        // Validación similar a reporteporgrupo
        if (mesinicio == null || mesfin == null) {
            throw new IllegalArgumentException("Los meses de inicio y fin no pueden ser nulos");
        }
        DateTimeFormatter formatoMes = DateTimeFormatter.ofPattern("yyyy-MM");
        YearMonth inicio;
        YearMonth fin;
        try {
            inicio = YearMonth.parse(mesinicio, formatoMes);
            fin = YearMonth.parse(mesfin, formatoMes);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha inválido, debe ser YYYY-MM");
        }
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("El mes de inicio debe ser anterior o igual al mes de fin");
        }

        // Obtener todas las reservas y comprobantes del servicio M5
        ResponseEntity<List<ReservaDTO>> reservasResponse = restTemplate.exchange(
                RESERVAS_SERVICE_URL + "/todas",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ReservaDTO>>() {}
        );
        List<ReservaDTO> reservas = reservasResponse.getBody();
        if (reservas == null) reservas = new ArrayList<>();

        ResponseEntity<List<ComprobanteDTO>> comprobantesResponse = restTemplate.exchange(
                COMPROBANTES_SERVICE_URL + "/todas",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ComprobanteDTO>>() {}
        );
        List<ComprobanteDTO> comprobantes = comprobantesResponse.getBody();
        if (comprobantes == null) comprobantes = new ArrayList<>();

        Map<Long, ComprobanteDTO> comprobantesMap = comprobantes.stream()
                .collect(Collectors.toMap(ComprobanteDTO::getIdreserva, c -> c));

        int mesesRango = (fin.getYear() - inicio.getYear()) * 12 + fin.getMonthValue() - inicio.getMonthValue() + 1;
        List<List<Long>> reporte = new ArrayList<>(mesesRango);

        YearMonth mesActual = inicio;
        for (int i = 0; i < mesesRango; i++) {
            LocalDateTime inicioMes = mesActual.atDay(1).atStartOfDay();
            LocalDateTime finMes = mesActual.atEndOfMonth().atTime(23, 59, 59);

            List<ReservaDTO> reservasMes = reservas.stream()
                    .filter(reserva -> {
                        LocalDateTime fechaReserva = LocalDateTime.parse(reserva.getFechahora(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                        return !fechaReserva.isBefore(inicioMes) && !fechaReserva.isAfter(finMes);
                    })
                    .collect(Collectors.toList());

            long totalTipo1 = 0L;
            long totalTipo2 = 0L;
            long totalTipo3 = 0L;

            for (ReservaDTO r : reservasMes) {
                ComprobanteDTO comprobante = comprobantesMap.get(r.getId());
                if (comprobante != null) {
                    long precioFinal = (long) comprobante.getPreciofinal();
                    if (r.getTiporeserva() == 1) {
                        totalTipo1 += precioFinal;
                    } else if (r.getTiporeserva() == 2) {
                        totalTipo2 += precioFinal;
                    } else if (r.getTiporeserva() == 3) {
                        totalTipo3 += precioFinal;
                    }
                }
            }
            reporte.add(List.of(totalTipo1, totalTipo2, totalTipo3));
            mesActual = mesActual.plusMonths(1);
        }
        return reporte;
    }
}
