package com.tingeso.tarifasservice.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tarifas")
@Data
@NoArgsConstructor
@AllArgsConstructor // Para el constructor con todos los argumentos
public class TarifaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int tipoReserva; // 1: 10 vueltas, 2: 15 vueltas, 3: 20 vueltas
    private float precioBase; // El precio regular de la reserva
    private int duracionMinutos; // Duración total de la reserva
    // Puedes añadir campos para el número de vueltas o tiempo máximo si lo necesitas para mayor claridad
    private String descripcion; // Ej: "10 vueltas o máx 10 min"

    public TarifaEntity(int tipoReserva, float precioBase, int duracionMinutos, String descripcion) {
        this.id = id;
        this.tipoReserva = tipoReserva;
        this.precioBase = precioBase;
        this.duracionMinutos = duracionMinutos;
        this.descripcion = descripcion;
    }

}
