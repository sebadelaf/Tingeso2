package com.tingeso.rackpistaservice.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name ="reservas")
@Data
@NoArgsConstructor
public class ReservaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String fechahora;
    private int tiporeserva;
    private int cantidadpersonas;
    private int cantidadcumple;
    private String nombreusuario;
    private String rutusuario;
    private String email;
    public ReservaEntity(int cantidadcumple, int cantidadpersonas, String email, String fechahora, String nombreusuario, String rutusuario, int tiporeserva) {
        this.cantidadcumple = cantidadcumple;
        this.cantidadpersonas = cantidadpersonas;
        this.email = email;
        this.fechahora = fechahora;
        this.id = id;
        this.nombreusuario = nombreusuario;
        this.rutusuario = rutusuario;
        this.tiporeserva = tiporeserva;
    }
}
