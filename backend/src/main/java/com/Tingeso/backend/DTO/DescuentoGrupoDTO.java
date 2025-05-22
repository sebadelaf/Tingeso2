package com.Tingeso.backend.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DescuentoGrupoDTO {
    private int cantidadpersonas;
    private float precioinicial;

    public DescuentoGrupoDTO(int cantidadpersonas, float precioinicial) {
        this.cantidadpersonas = cantidadpersonas;
        this.precioinicial = precioinicial;
    }
}
