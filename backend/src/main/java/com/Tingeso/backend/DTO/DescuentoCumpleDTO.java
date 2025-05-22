package com.Tingeso.backend.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DescuentoCumpleDTO {
    private int cantidadpersonas;
    private float precioinicial;
    private int cantidadcumple;

    public DescuentoCumpleDTO(int cantidadpersonas, float precioinicial, int cantidadcumple) {
        this.cantidadpersonas = cantidadpersonas;
        this.precioinicial = precioinicial;
        this.cantidadcumple = cantidadcumple;
    }
}
