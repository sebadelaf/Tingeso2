package com.tingeso.reportesservice.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaDTO {
    private Long id;
    private String fechahora;
    private int tiporeserva;
    private int cantidadpersonas;
    private int cantidadcumple;
    private String nombreusuario;
    private String rutusuario;
    private String email;
}
