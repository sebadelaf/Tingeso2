package com.tingeso.reservascomprobantesservice.Service;

import com.tingeso.reservascomprobantesservice.Entity.ReservaEntity;
import com.tingeso.reservascomprobantesservice.Repository.ReservaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

@Service
public class ReservaService {
    @Autowired
    public ReservaRepository reservaRepository;

    @Transactional
    // creador de la reserva segun los valores recibidos del formulario
    public ReservaEntity crearReserva(String fechahora, int tiporeserva, int cantidadpersonas, int cantidadcumple, String nombreusuario, String rutusuario, String email) {
        // Validaciones básicas existentes
        if (fechahora == null || fechahora.isEmpty()) {
            throw new IllegalArgumentException("La fecha y hora no pueden estar vacías");
        }
        if (tiporeserva < 0 || cantidadpersonas <= 0 || tiporeserva > 3) {
            throw new IllegalArgumentException("Tipo de reserva no puede ser negativo o mayor que 3 y cantidad de personas debe ser mayor a 0");
        }
        if (nombreusuario == null || nombreusuario.isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacío");
        }
        if (rutusuario == null || rutusuario.isEmpty()) {
            throw new IllegalArgumentException("El RUT de usuario no puede estar vacío");
        }
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        if (cantidadcumple < 0) {
            throw new IllegalArgumentException("Cantidad de cumpleaños no puede ser negativa");
        }

        // Parsear la fecha y hora de inicio
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime fechaInicio;
        try {
            fechaInicio = LocalDateTime.parse(fechahora, formato);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha y hora inválido: " + fechahora);
        }

        // Determinar el día de la semana y si es feriado
        DayOfWeek diaSemana = fechaInicio.getDayOfWeek();
        DateTimeFormatter formatoMesDia = DateTimeFormatter.ofPattern("MM-dd");
        String fechaMesDia = fechaInicio.format(formatoMesDia);
        List<String> feriados = Arrays.asList("01-01", "05-01", "09-18", "09-19", "12-25");
        boolean esFeriado = feriados.contains(fechaMesDia);

        // Definir horarios de atención
        LocalTime apertura;
        LocalTime cierre = LocalTime.of(22, 0); // 22:00 para todos los días
        if (esFeriado || diaSemana == DayOfWeek.SATURDAY || diaSemana == DayOfWeek.SUNDAY) {
            apertura = LocalTime.of(10, 0); // 10:00 para sábados, domingos y feriados
        } else {
            apertura = LocalTime.of(14, 0); // 14:00 para lunes a viernes
        }

        // Validar que la hora de inicio esté dentro del horario de atención
        LocalTime horaInicio = fechaInicio.toLocalTime();
        if (horaInicio.isBefore(apertura) || horaInicio.isAfter(cierre)) {
            throw new IllegalArgumentException("La hora de inicio está fuera del horario de atención: " + apertura + " a " + cierre);
        }

        // Calcular la duración según el tipo de reserva
        int duracionMinutos;
        switch (tiporeserva) {
            case 1:
                duracionMinutos = 30;
                break;
            case 2:
                duracionMinutos = 35;
                break;
            case 3:
                duracionMinutos = 40;
                break;
            default:
                throw new IllegalArgumentException("Tipo de reserva inválido: " + tiporeserva);
        }

        // Calcular la hora de término
        LocalDateTime fechaTermino = fechaInicio.plusMinutes(duracionMinutos);

        // Validar que la hora de término no exceda el cierre
        if (fechaTermino.toLocalTime().isAfter(cierre)) {
            throw new IllegalArgumentException("La reserva excede el horario de cierre (" + cierre + ")");
        }

        // Obtener todas las reservas existentes
        List<ReservaEntity> reservasExistentes = reservaRepository.findAll();

        // Verificar superposición con reservas existentes
        for (ReservaEntity existente : reservasExistentes) {
            LocalDateTime inicioExistente = LocalDateTime.parse(existente.getFechahora(), formato);
            int duracionExistente;
            switch (existente.getTiporeserva()) {
                case 1:
                    duracionExistente = 30;
                    break;
                case 2:
                    duracionExistente = 35;
                    break;
                case 3:
                    duracionExistente = 40;
                    break;
                default:
                    duracionExistente = 0; // No debería ocurrir debido a validaciones previas
            }
            LocalDateTime terminoExistente = inicioExistente.plusMinutes(duracionExistente);

            // Verificar si hay superposición
            if (fechaInicio.isBefore(terminoExistente) && fechaTermino.isAfter(inicioExistente)) {
                throw new IllegalArgumentException("La reserva se superpone con una existente desde " +
                        inicioExistente.format(formato) + " hasta " + terminoExistente.format(formato));
            }
        }

        // Crear y guardar la nueva reserva
        ReservaEntity reserva = new ReservaEntity(
                cantidadcumple,cantidadpersonas,email,fechahora,nombreusuario,rutusuario,tiporeserva
        );
        reserva = reservaRepository.save(reserva);
        return reserva;
    }

    // metodo para obtener las reservas del usuario sera importante para verificar si este es fiel o no
    @Transactional
    public List<ReservaEntity> obtenerReservasUsuario(String rutuser) {
        return reservaRepository.findAllByRutusuario(rutuser);
    }

}
