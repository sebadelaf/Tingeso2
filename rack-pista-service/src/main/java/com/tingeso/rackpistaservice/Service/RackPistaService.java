package com.tingeso.rackpistaservice.Service;

import com.tingeso.rackpistaservice.Entity.BloqueHorarioEntity;
import com.tingeso.rackpistaservice.Repository.BloqueHorarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate; // Para llamar a M5
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
public class RackPistaService {

    @Autowired
    private BloqueHorarioRepository bloqueHorarioRepository;

    @Autowired
    private RestTemplate restTemplate; // Para obtener detalles de la reserva de M5

    // Necesitamos un DTO para la ReservaEntity de M5, ya que no podemos importar la entidad directamente
    // Define esta clase dentro de RackPistaService o en un nuevo paquete DTO
    @Data // Lombok annotation
    @NoArgsConstructor // Lombok annotation
    @AllArgsConstructor // Lombok annotation
    public static class ReservaDTO {
        private Long id;
        private String fechahora;
        private int tiporeserva;
        private int cantidadpersonas;
        private int cantidadcumple;
        private String nombreusuario;
        private String rutusuario;
        private String email;
    }


    // Método para registrar un bloque horario basado en una reserva de M5
    @Transactional
    public BloqueHorarioEntity registrarBloqueDesdeReserva(Long idReserva) {
        // 1. Obtener la Reserva de M5 (reservas-comprobantes-service)
        String reservaServiceUrl = "http://localhost:8080/reservas/obtenerById/" + idReserva;
        ReservaDTO reserva = null;
        try {
            reserva = restTemplate.getForObject(reservaServiceUrl, ReservaDTO.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("No se pudo obtener la reserva con ID: " + idReserva + " desde el servicio de reservas. Error: " + e.getMessage());
        }

        if (reserva == null) {
            throw new IllegalArgumentException("Reserva no encontrada en el servicio de reservas con ID: " + idReserva);
        }

        // 2. Calcular inicio y fin del bloque (lógica copiada de ReservaService original)
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime fechaInicio;
        try {
            fechaInicio = LocalDateTime.parse(reserva.getFechahora(), formato);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha y hora inválido en la reserva: " + reserva.getFechahora());
        }

        int duracionMinutos;
        switch (reserva.getTiporeserva()) {
            case 1: duracionMinutos = 30; break;
            case 2: duracionMinutos = 35; break;
            case 3: duracionMinutos = 40; break;
            default: throw new IllegalArgumentException("Tipo de reserva inválido en la reserva: " + reserva.getTiporeserva());
        }
        LocalDateTime fechaFin = fechaInicio.plusMinutes(duracionMinutos);

        // 3. Verificar superposiciones dentro de este mismo microservicio (Rack)
        // Esto es crucial para la integridad del rack
        List<BloqueHorarioEntity> superposiciones = bloqueHorarioRepository.findByInicioBloqueBetweenOrFinBloqueBetweenOrInicioBloqueLessThanEqualAndFinBloqueGreaterThanEqual(
                fechaInicio, fechaFin, fechaInicio, fechaFin, fechaInicio, fechaFin
        );

        for (BloqueHorarioEntity existente : superposiciones) {
            // Asegúrate de que no sea la misma reserva si se está actualizando
            if (!existente.getIdReserva().equals(idReserva)) {
                throw new IllegalArgumentException("El bloque horario se superpone con una reserva existente en el rack (ID: " + existente.getIdReserva() + ")");
            }
        }

        // 4. Guardar o actualizar el BloqueHorario
        BloqueHorarioEntity bloqueExistente = bloqueHorarioRepository.findByIdReserva(idReserva);
        if (bloqueExistente != null) {
            // Si ya existe, actualiza
            bloqueExistente.setInicioBloque(fechaInicio);
            bloqueExistente.setFinBloque(fechaFin);
            bloqueExistente.setTipoReserva(reserva.getTiporeserva());
            bloqueExistente.setCantidadPersonas(reserva.getCantidadpersonas());
            bloqueExistente.setNombreCliente(reserva.getNombreusuario());
            bloqueExistente.setEstado("OCUPADO"); // Asegurarse de que el estado es ocupado si se registra/actualiza
            return bloqueHorarioRepository.save(bloqueExistente);
        } else {
            // Si no existe, crea uno nuevo
            BloqueHorarioEntity nuevoBloque = new BloqueHorarioEntity(
                    null,
                    idReserva,
                    reserva.getNombreusuario(),
                    fechaInicio,
                    fechaFin,
                    reserva.getTiporeserva(),
                    reserva.getCantidadpersonas(),
                    "OCUPADO"
            );
            return bloqueHorarioRepository.save(nuevoBloque);
        }
    }

    @Transactional
    public BloqueHorarioEntity cancelarBloque(Long idReserva) {
        BloqueHorarioEntity bloque = bloqueHorarioRepository.findByIdReserva(idReserva);
        if (bloque != null) {
            bloque.setEstado("CANCELADO");
            return bloqueHorarioRepository.save(bloque);
        }
        throw new IllegalArgumentException("Bloque horario no encontrado para la reserva con ID: " + idReserva);
    }

    public List<BloqueHorarioEntity> obtenerRackSemanal(String fechaInicioStr) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime fechaInicio;
        try {
            fechaInicio = LocalDateTime.parse(fechaInicioStr + "T00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha inválido para el inicio del rack (YYYY-MM-DD): " + fechaInicioStr);
        }
        LocalDateTime fechaFin = fechaInicio.plusDays(6).withHour(23).withMinute(59).withSecond(59); // Una semana completa

        return bloqueHorarioRepository.findByInicioBloqueBetweenOrderByInicioBloqueAsc(fechaInicio, fechaFin);
    }

    public List<BloqueHorarioEntity> obtenerTodosLosBloques() {
        return bloqueHorarioRepository.findAll();
    }
}
