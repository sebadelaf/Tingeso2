package com.Tingeso.backend.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DescuentoEspecialDTO {
    private long iduser;
    private float precioinicial;

    public DescuentoEspecialDTO(long iduser, float precioinicial) {
        this.iduser = iduser;
        this.precioinicial = precioinicial;
    }
}
