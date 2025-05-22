package com.Tingeso.backend.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "comprobantes")
@Data
@NoArgsConstructor
public class ComprobanteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long idreserva;
    private float tarifabase;
    private float dctogrupo;
    private float dctoespecial;
    private float dctocumple;
    private float precio;
    private float valoriva;
    private float preciofinal;

    public ComprobanteEntity(float dctocumple,float dctoespecial, float dctogrupo, long idreserva, float precio, float preciofinal, float tarifabase, float valoriva) {
        this.dctoespecial = dctoespecial;
        this.dctogrupo = dctogrupo;
        this.id = id;
        this.idreserva = idreserva;
        this.precio = precio;
        this.preciofinal = preciofinal;
        this.tarifabase = tarifabase;
        this.valoriva = valoriva;
        this.dctocumple = dctocumple;
    }
}
