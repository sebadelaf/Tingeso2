package com.tingeso.descuentosfrecuentesservice.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "visitas_cliente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String rutCliente;
    private LocalDateTime fechaVisita; // Fecha y hora de la visita
}
