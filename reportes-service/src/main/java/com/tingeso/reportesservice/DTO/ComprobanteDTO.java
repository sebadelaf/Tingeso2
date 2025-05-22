package com.tingeso.reportesservice.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteDTO {
    private Long id;
    private Long idreserva;
    private float tarifabase;
    private float dctogrupo;
    private float dctoespecial;
    private float dctocumple;
    private float precio; // Este es precio inicial en tu entidad
    private float valoriva;
    private float preciofinal; // Este es precio final con iva
}
