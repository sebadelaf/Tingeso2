package com.Tingeso.backend.Service;

import com.Tingeso.backend.Entity.ReservaEntity;
import com.Tingeso.backend.Repository.ReservaRepository;
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

    // metodo para obtener el precio incial de la reserva en relacion a las tarifas especiales de dias feriados y fines de semana
    public float calcularprecioinicial(long idreserva) {
        ReservaEntity reserva = reservaRepository.findById(idreserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + idreserva));
        int tiporeserva = reserva.getTiporeserva();
        int cantidadpersonas= reserva.getCantidadpersonas();
        float precioinicial = 0;
        if(tiporeserva == 1) {
            precioinicial = 15000*cantidadpersonas;
        } else if (tiporeserva == 2) {
            precioinicial = 20000*cantidadpersonas;
        } else if (tiporeserva == 3) {
            precioinicial = 25000*cantidadpersonas;
        }

        //formatos para la fecha
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime fechaInicio = LocalDateTime.parse(reserva.getFechahora(), formato);
        DateTimeFormatter formatoMesDia = DateTimeFormatter.ofPattern("MM-dd");
        String fechaInicioMesDia = fechaInicio.format(formatoMesDia);
        List<String> feriados = Arrays.asList("01-01", "05-01", "09-18", "09-19", "12-25");

        // Verificar si la fecha de inicio es un feriado
        boolean esFeriado = feriados.contains(fechaInicioMesDia);
        if (esFeriado) {
            precioinicial= precioinicial - (0.25f*precioinicial);
        }
        // Verificar si la fecha de inicio es un fin de semana
        if (fechaInicio.getDayOfWeek().getValue() == 6 || fechaInicio.getDayOfWeek().getValue() == 7) {
            precioinicial= precioinicial - (0.15f*precioinicial);
        }
        return precioinicial;
    }


    public float calcularDescuentoGrupo(int cantidadpersonas, float precioinicial) {
        if (cantidadpersonas <= 2) {
            return 0f; // Sin descuento
        } else if (cantidadpersonas <= 5) {
            return 0.1f * precioinicial; // 10% descuento
        } else if (cantidadpersonas <= 10) {
            return 0.2f * precioinicial; // 20% descuento
        } else if (cantidadpersonas <= 15) {
            return 0.3f * precioinicial; // 30% descuento
        }
        return 0f; // Sin descuento fuera del rango
    }

    @Transactional
    public float calcularDescuentoEspecial(String rutuser,float precioinicial) {
        List<ReservaEntity> reservas = reservaRepository.findAllByRutusuario(rutuser);
        LocalDateTime ahora = LocalDateTime.now();
        int mesActual = ahora.getMonthValue();
        int anioActual = ahora.getYear();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        long reservasEnMes = reservas.stream()
                .map(reserva -> LocalDateTime.parse(reserva.getFechahora(), formato))
                .filter(fecha -> fecha.getMonthValue() == mesActual && fecha.getYear() == anioActual)
                .count();

        if (reservasEnMes <= 1) {
            return 0f; // Sin descuento
        } else if (reservasEnMes <= 4) {
            return 0.1f * precioinicial; // 10% descuento
        } else if (reservasEnMes <= 6) {
            return 0.2f * precioinicial; // 20% descuento
        } else {
            return 0.3f * precioinicial; // 30% descuento
        }
    }

    public float descuentoporcumpleano(int cantidadpersonas, float precioinicial, int cantidadcumple) {
        if (cantidadcumple>0){
            float dctocumple = 0;
            float tarifa=precioinicial/cantidadpersonas;
            if (cantidadpersonas>=3 && cantidadpersonas<=5){
                dctocumple = tarifa*0.5f;
            }else if (cantidadpersonas>=6){
                dctocumple = tarifa*0.5f*2;}
            return dctocumple;
        }
        return 0f;
    }   
}
