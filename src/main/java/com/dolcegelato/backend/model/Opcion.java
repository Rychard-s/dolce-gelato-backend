package com.dolcegelato.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

// RN-04: opciones que se eligen en un producto.
// Ej: producto Alitas · grupo "Sabor" · nombre "BBQ" · obligatorio · maxElegir 1
//     producto Waffle  · grupo "Frutas" · nombre "Fresa" · obligatorio · maxElegir 2
//     producto Hamburguesa · grupo "Salsas" · nombre "Mayonesa" · opcional · maxElegir 3
@Entity
@Table(name = "opciones",
       uniqueConstraints = @UniqueConstraint(columnNames = {"producto_id", "grupo", "nombre"}))
@Getter @Setter
@NoArgsConstructor
public class Opcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El producto es obligatorio")
    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @NotBlank(message = "El grupo es obligatorio (ej. Sabor, Presa, Salsas, Frutas)")
    @Size(max = 40, message = "El grupo no debe superar 40 caracteres")
    @Column(nullable = false, length = 40)
    private String grupo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no debe superar 60 caracteres")
    @Column(nullable = false, length = 60)
    private String nombre;

    // Si es obligatorio, hay que elegir EXACTAMENTE maxElegir opciones del grupo.
    // Si no, se puede elegir de 0 hasta maxElegir.
    private boolean obligatorio;

    @Min(value = 1, message = "maxElegir debe ser al menos 1")
    @Max(value = 10, message = "maxElegir no puede ser mayor a 10")
    private int maxElegir = 1;

    // RN-02: una opción agotada no se puede elegir
    private boolean disponible = true;
}