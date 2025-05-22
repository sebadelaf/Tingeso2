package com.tingeso.tarifasespecialesservice.Service;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

@Service
public class TarifaEspecialService {

    // Lista de feriados fijos (MM-dd)
    private static final List<String> FERIADOS = Arrays.asList("01-01", "05-01", "09-18", "09-19", "12-25");

    // Migración y adaptación de la lógica de calcularprecioinicial (parte de descuentos/tarifas)
    public float aplicarDescuentoPorDiaEspecial(String fechahora, float precioInicialBase) {
        LocalDateTime fechaInicio;
        try {
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            fechaInicio = LocalDateTime.parse(fechahora, formato);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha y hora inválido: " + fechahora);
        }

        float precioConDescuento = precioInicialBase;

        // Verificar si la fecha de inicio es un feriado [cite: 27, 28]
        DateTimeFormatter formatoMesDia = DateTimeFormatter.ofPattern("MM-dd");
        String fechaMesDia = fechaInicio.format(formatoMesDia);
        boolean esFeriado = FERIADOS.contains(fechaMesDia);
        if (esFeriado) {
            precioConDescuento = precioConDescuento - (0.25f * precioConDescuento); // Asumimos un 25% de descuento en feriados (como en tu código original)
        }
        // Verificar si la fecha de inicio es un fin de semana [cite: 27]
        if (fechaInicio.getDayOfWeek() == DayOfWeek.SATURDAY || fechaInicio.getDayOfWeek() == DayOfWeek.SUNDAY) {
            precioConDescuento = precioConDescuento - (0.15f * precioConDescuento); // Asumimos un 15% de descuento en fines de semana (como en tu código original)
        }

        return precioConDescuento;
    }

    // Migración y adaptación de la lógica de descuentoporcumpleano
    public float calcularDescuentoCumpleanos(int cantidadPersonas, float precioInicialOriginal, int cantidadCumple) {
        if (cantidadCumple > 0) {
            float tarifaPorPersona = precioInicialOriginal / cantidadPersonas;
            if (cantidadPersonas >= 3 && cantidadPersonas <= 5) {
                return tarifaPorPersona * 0.5f; // 50% de descuento para una persona [cite: 29, 30]
            } else if (cantidadPersonas >= 6) {
                return tarifaPorPersona * 0.5f * 2; // 50% de descuento para hasta dos personas [cite: 30]
            }
        }
        return 0f;
    }
}