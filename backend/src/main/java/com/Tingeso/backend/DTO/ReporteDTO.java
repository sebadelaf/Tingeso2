package com.Tingeso.backend.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReporteDTO {
    private String mesinicio;
    private String mesfin;

    public ReporteDTO(String mesinicio, String mesfin) {
        this.mesinicio = mesinicio;
        this.mesfin = mesfin;
    }
}
