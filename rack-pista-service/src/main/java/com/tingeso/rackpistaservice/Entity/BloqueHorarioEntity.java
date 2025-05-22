package com.tingeso.rackpistaservice.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Entity
@Table(name = "bloques_horarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BloqueHorarioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long idReserva; // ID de la reserva asociada
    private String nombreCliente;
    private LocalDateTime inicioBloque;
    private LocalDateTime finBloque;
    private int tipoReserva; // 1, 2, 3 (para referencia rápida)
    private int cantidadPersonas;
    private String estado; // Ej: "OCUPADO", "CANCELADO"
}