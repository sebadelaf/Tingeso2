package com.tingeso.descuentosgruposervice.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "descuentos_grupo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DescuentoGrupoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int minPersonas;
    private int maxPersonas;
    private float porcentajeDescuento; // 0.0f a 1.0f (ej: 0.1f para 10%)
    private String descripcion; // Ej: "1-2 personas", "3-5 personas"
}
